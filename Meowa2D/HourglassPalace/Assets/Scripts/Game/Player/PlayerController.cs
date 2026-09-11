using System.Collections.Generic;
using UnityEngine;
using UnityEngine.InputSystem;

namespace HourglassPalace
{
    /// <summary>
    /// Miaobaibai.  Few verbs, read deeply: walk, run, jump, hang, climb, one
    /// scimitar swing, one interact.  Collision is deliberately separate from
    /// the sprite so the body can be tuned in the inspector without touching art.
    /// </summary>
    [RequireComponent(typeof(Rigidbody2D))]
    [RequireComponent(typeof(CapsuleCollider2D))]
    public sealed class PlayerController : MonoBehaviour
    {
        public enum MoveState { Idle, Walk, Run, Rise, Fall, Hang, ClimbUp, Chain, Attack, Hurt, Dead, Busy }

        [Header("Body (visual and collision are separate)")]
        public Vector2 bodySize = new Vector2(0.9f, 1.9f);
        public LayerMask solidMask;
        public float groundProbe = 0.14f;

        [Header("Ground movement")]
        public float walkSpeed = 4.2f;
        public float runSpeed = 7.4f;
        public float groundAccel = 70f;
        public float groundBrakeWalk = 90f;
        public float groundBrakeRun = 36f;   // running costs you stopping distance
        public float airAccel = 34f;

        [Header("Jump")]
        public float jumpSpeed = 15.5f;
        public float riseGravity = 4.6f;
        public float fallGravity = 5.6f;
        public float maxFallSpeed = 24f;
        public float coyoteTime = 0.1f;
        public float jumpBuffer = 0.12f;

        [Header("Ledge hang")]
        public float ledgeReach = 0.3f;
        public float handHeight = 1.56f;
        public float headClearance = 0.5f;
        public float regrabDelay = 0.3f;
        public float climbUpTime = 0.32f;

        [Header("Chain climb")]
        public float chainSpeed = 3.4f;

        [Header("Bronze scimitar")]
        public float attackWindup = 0.1f;
        public float attackActive = 0.16f;
        public float attackRecover = 0.3f;
        public Vector2 bladeSize = new Vector2(1.3f, 1.1f);
        public float bladeForward = 0.9f;
        public float bladeHeight = 1.0f;

        [Header("Life")]
        public int maxHealth = 3;
        public float invulnTime = 0.45f;
        public float hurtLock = 0.26f;
        public Vector2 hurtKnockback = new Vector2(4.2f, 5.4f);
        [Tooltip("Landing faster than this is a fatal fall (about a four unit drop).")]
        public float fatalFallSpeed = 21f;

        [Header("Interaction")]
        public float interactRadius = 1.25f;

        Rigidbody2D _rb;
        CapsuleCollider2D _body;
        readonly Collider2D[] _overlap = new Collider2D[16];
        readonly HashSet<Object> _swingHits = new HashSet<Object>();

        float _moveInput;
        bool _runHeld;
        bool _jumpPressed;
        bool _attackPressed;
        bool _interactPressed;
        float _verticalInput;
        bool _upPressed;
        bool _downPressed;

        float _coyote;
        float _buffer;
        float _regrab;
        float _invuln;
        float _lockTimer;
        float _attackTimer;
        float _climbUpTimer;
        float _fallSpeed;
        int _facing = 1;

        MoveState _state = MoveState.Idle;
        ClimbChain _chain;
        Vector2 _climbFrom;
        Vector2 _climbTo;
        WorldInteractable _prompt;

        public int Health { get; private set; }
        public bool Dead => _state == MoveState.Dead;
        public int Facing => _facing;
        public MoveState State => _state;
        public bool Grounded { get; private set; }
        public bool BladeActive { get; private set; }
        public bool Invulnerable => _invuln > 0f;
        public WorldInteractable Prompt => _prompt;
        public bool Running => _runHeld;
        public bool CanUseConsumable => _state != MoveState.Hurt && _state != MoveState.Dead && Grounded;
        public Vector2 Center => (Vector2)transform.position + Vector2.up * (bodySize.y * 0.5f);
        public Vector2 Velocity => _rb != null ? _rb.linearVelocity : Vector2.zero;

        void Awake()
        {
            _rb = GetComponent<Rigidbody2D>();
            _body = GetComponent<CapsuleCollider2D>();
            _body.direction = CapsuleDirection2D.Vertical;
            _body.size = bodySize;
            _body.offset = new Vector2(0f, bodySize.y * 0.5f);
            _rb.freezeRotation = true;
            _rb.gravityScale = riseGravity;
            _rb.interpolation = RigidbodyInterpolation2D.Interpolate;
            _rb.collisionDetectionMode = CollisionDetectionMode2D.Continuous;
            Health = maxHealth;
        }

        void Update()
        {
            var kb = Keyboard.current;
            if (kb == null) return;

            _moveInput = 0f;
            if (kb.aKey.isPressed || kb.leftArrowKey.isPressed) _moveInput -= 1f;
            if (kb.dKey.isPressed || kb.rightArrowKey.isPressed) _moveInput += 1f;
            _verticalInput = 0f;
            if (kb.wKey.isPressed || kb.upArrowKey.isPressed) _verticalInput += 1f;
            if (kb.sKey.isPressed || kb.downArrowKey.isPressed) _verticalInput -= 1f;
            _runHeld = kb.leftShiftKey.isPressed || kb.rightShiftKey.isPressed;

            if (kb.spaceKey.wasPressedThisFrame) { _jumpPressed = true; _buffer = jumpBuffer; }
            if (kb.jKey.wasPressedThisFrame) _attackPressed = true;
            if (kb.eKey.wasPressedThisFrame) _interactPressed = true;
            if (kb.wKey.wasPressedThisFrame || kb.upArrowKey.wasPressedThisFrame) _upPressed = true;
            if (kb.sKey.wasPressedThisFrame || kb.downArrowKey.wasPressedThisFrame) _downPressed = true;
        }

        void FixedUpdate()
        {
            float dt = Time.fixedDeltaTime;
            TickTimers(dt);

            if (GameDirector.Paused || _state == MoveState.Dead)
            {
                if (_state != MoveState.Dead) _rb.linearVelocity = Vector2.zero;
                ConsumeEdges();
                return;
            }

            UpdatePrompt();
            if (_interactPressed) TryInteract();

            switch (_state)
            {
                case MoveState.Hang: TickHang(); break;
                case MoveState.ClimbUp: TickClimbUp(dt); break;
                case MoveState.Chain: TickChain(dt); break;
                case MoveState.Busy: _rb.linearVelocity = Vector2.zero; break;
                default: TickGroundAir(dt); break;
            }

            ConsumeEdges();
        }

        void TickTimers(float dt)
        {
            if (_invuln > 0f) _invuln -= dt;
            if (_regrab > 0f) _regrab -= dt;
            if (_buffer > 0f) _buffer -= dt;
            if (_lockTimer > 0f)
            {
                _lockTimer -= dt;
                if (_lockTimer <= 0f && (_state == MoveState.Hurt || _state == MoveState.Busy))
                    _state = MoveState.Idle;
            }
        }

        void ConsumeEdges()
        {
            _jumpPressed = false;
            _attackPressed = false;
            _interactPressed = false;
            _upPressed = false;
            _downPressed = false;
        }

        // ---- ground and air ------------------------------------------------

        void TickGroundAir(float dt)
        {
            bool wasGrounded = Grounded;
            Grounded = ProbeGround();
            var v = _rb.linearVelocity;

            if (!wasGrounded && Grounded)
            {
                if (_fallSpeed > fatalFallSpeed) { Die(); return; }
                _fallSpeed = 0f;
            }
            if (!Grounded) _fallSpeed = Mathf.Max(_fallSpeed, -v.y);

            if (Grounded) _coyote = coyoteTime;
            else _coyote = Mathf.Max(0f, _coyote - dt);

            bool locked = _state == MoveState.Hurt;
            if (!locked && _moveInput != 0f && _attackTimer <= 0f)
                SetFacing(_moveInput > 0f ? 1 : -1);

            if (_attackPressed && _attackTimer <= 0f && !locked) StartAttack();
            if (_attackTimer > 0f) TickAttack(dt);

            float target;
            if (locked) target = v.x;
            else if (_attackTimer > 0f) target = _moveInput * walkSpeed * 0.25f;   // the swing plants the feet
            else target = _moveInput * (_runHeld ? runSpeed : walkSpeed);

            if (!locked)
            {
                float accel = Grounded
                    ? (Mathf.Abs(target) > 0.01f ? groundAccel : (_runHeld ? groundBrakeRun : groundBrakeWalk))
                    : airAccel;
                v.x = Mathf.MoveTowards(v.x, target, accel * dt);
            }

            if (_buffer > 0f && _coyote > 0f && !locked)
            {
                v.y = jumpSpeed;
                _buffer = 0f;
                _coyote = 0f;
                Grounded = false;
                _fallSpeed = 0f;
            }

            _rb.gravityScale = v.y > 0.1f ? riseGravity : fallGravity;
            if (v.y < -maxFallSpeed) v.y = -maxFallSpeed;
            _rb.linearVelocity = v;

            if (!Grounded && v.y < -0.5f && _regrab <= 0f && _attackTimer <= 0f && !locked)
                TryGrabLedge();

            if (_state == MoveState.Hurt || _state == MoveState.Busy) return;
            if (_state == MoveState.Hang || _state == MoveState.ClimbUp) return;
            if (_attackTimer > 0f) { _state = MoveState.Attack; return; }
            if (!Grounded) _state = v.y > 0.1f ? MoveState.Rise : MoveState.Fall;
            else if (Mathf.Abs(v.x) < 0.25f) _state = MoveState.Idle;
            else _state = _runHeld && Mathf.Abs(v.x) > walkSpeed * 1.05f ? MoveState.Run : MoveState.Walk;
        }

        bool ProbeGround()
        {
            var origin = (Vector2)transform.position + Vector2.up * (groundProbe * 0.5f);
            var size = new Vector2(bodySize.x * 0.92f, groundProbe);
            return Physics2D.OverlapBox(origin, size, 0f, solidMask) != null;
        }

        // ---- ledge hang ----------------------------------------------------

        void TryGrabLedge()
        {
            int dir = _facing;
            float reach = bodySize.x * 0.5f + ledgeReach;
            var handOrigin = (Vector2)transform.position + Vector2.up * handHeight;
            var wall = Physics2D.Raycast(handOrigin, Vector2.right * dir, reach, solidMask);
            if (!wall) return;
            var above = Physics2D.Raycast(handOrigin + Vector2.up * headClearance, Vector2.right * dir, reach, solidMask);
            if (above) return;

            var probe = handOrigin + Vector2.right * dir * (reach + 0.08f) + Vector2.up * headClearance;
            var top = Physics2D.Raycast(probe, Vector2.down, headClearance + 0.35f, solidMask);
            if (!top) return;

            float feetY = top.point.y - handHeight;
            float x = wall.point.x - dir * (bodySize.x * 0.5f + 0.02f);
            transform.position = new Vector3(x, feetY, transform.position.z);
            _rb.linearVelocity = Vector2.zero;
            _rb.gravityScale = 0f;
            _fallSpeed = 0f;
            _state = MoveState.Hang;
        }

        void TickHang()
        {
            _rb.linearVelocity = Vector2.zero;
            _rb.gravityScale = 0f;
            if (_downPressed) { ReleaseHang(); return; }
            if (_upPressed || _jumpPressed)
            {
                _climbFrom = transform.position;
                _climbTo = _climbFrom + new Vector2(_facing * (bodySize.x * 0.65f), handHeight + 0.06f);
                _climbUpTimer = 0f;
                _state = MoveState.ClimbUp;
            }
        }

        void TickClimbUp(float dt)
        {
            _climbUpTimer += dt;
            float t = Mathf.Clamp01(_climbUpTimer / Mathf.Max(0.01f, climbUpTime));
            // up first, then over: reads as a cat pulling itself onto a ledge
            float x = Mathf.Lerp(_climbFrom.x, _climbTo.x, Mathf.SmoothStep(0f, 1f, Mathf.Clamp01((t - 0.45f) / 0.55f)));
            float y = Mathf.Lerp(_climbFrom.y, _climbTo.y, Mathf.SmoothStep(0f, 1f, Mathf.Clamp01(t / 0.7f)));
            _rb.linearVelocity = Vector2.zero;
            transform.position = new Vector3(x, y, transform.position.z);
            if (t >= 1f)
            {
                _rb.gravityScale = fallGravity;
                _state = MoveState.Idle;
                Grounded = true;
                _regrab = regrabDelay;
            }
        }

        void ReleaseHang()
        {
            _rb.gravityScale = fallGravity;
            _regrab = regrabDelay;
            _fallSpeed = 0f;
            _state = MoveState.Fall;
        }

        // ---- chains --------------------------------------------------------

        public void EnterChain(ClimbChain chain)
        {
            if (chain == null || Dead || _state == MoveState.Chain) return;
            if (_regrab > 0f) return;
            _chain = chain;
            _state = MoveState.Chain;
            _rb.gravityScale = 0f;
            _rb.linearVelocity = Vector2.zero;
            _fallSpeed = 0f;
        }

        public void ExitChain(ClimbChain chain)
        {
            if (_chain == chain) LeaveChain(false);
        }

        void TickChain(float dt)
        {
            if (_chain == null) { LeaveChain(false); return; }
            var pos = transform.position;
            pos.x = Mathf.MoveTowards(pos.x, _chain.transform.position.x, 6f * dt);
            transform.position = pos;
            _rb.gravityScale = 0f;
            _rb.linearVelocity = new Vector2(0f, _verticalInput * chainSpeed);

            if (_jumpPressed || Mathf.Abs(_moveInput) > 0.1f) { LeaveChain(true); return; }
            if (!_chain.Contains(Center)) LeaveChain(false);
        }

        void LeaveChain(bool push)
        {
            _chain = null;
            _rb.gravityScale = fallGravity;
            _regrab = regrabDelay;
            if (push)
            {
                if (Mathf.Abs(_moveInput) > 0.1f) SetFacing(_moveInput > 0f ? 1 : -1);
                _rb.linearVelocity = new Vector2(_moveInput * walkSpeed, jumpSpeed * 0.62f);
            }
            _state = MoveState.Fall;
        }

        // ---- the scimitar ---------------------------------------------------

        void StartAttack()
        {
            _attackTimer = attackWindup + attackActive + attackRecover;
            _swingHits.Clear();
            BladeActive = false;
            _state = MoveState.Attack;
        }

        void TickAttack(float dt)
        {
            _attackTimer -= dt;
            float total = attackWindup + attackActive + attackRecover;
            float elapsed = total - _attackTimer;
            BladeActive = elapsed >= attackWindup && elapsed <= attackWindup + attackActive;
            if (BladeActive) ResolveBlade();
            if (_attackTimer <= 0f)
            {
                _attackTimer = 0f;
                BladeActive = false;
                if (_state == MoveState.Attack) _state = MoveState.Idle;
            }
        }

        void ResolveBlade()
        {
            var center = (Vector2)transform.position + new Vector2(bladeForward * _facing, bladeHeight);
            int count = Physics2D.OverlapBoxNonAlloc(center, bladeSize, 0f, _overlap);
            for (int i = 0; i < count; i++)
            {
                var col = _overlap[i];
                if (col == null || col.transform == transform) continue;
                var enemy = col.GetComponentInParent<EnemyBase>();
                if (enemy == null || enemy.Downed || _swingHits.Contains(enemy)) continue;
                _swingHits.Add(enemy);
                if (enemy.BladeActive)
                {
                    // Blades meet: both fighters are shoved apart, nobody scores.
                    Clash(enemy.transform.position.x > transform.position.x ? 1 : -1);
                    enemy.Clash();
                }
                else enemy.TakeHit(1, _facing, false);
            }
        }

        public void Clash(int fromDir)
        {
            _attackTimer = Mathf.Min(_attackTimer, attackRecover);
            BladeActive = false;
            _rb.linearVelocity = new Vector2(-fromDir * 4.5f, 2.2f);
            HitStop.Punch(0.09f);
        }

        // ---- interaction -----------------------------------------------------

        void UpdatePrompt()
        {
            WorldInteractable best = null;
            float bestScore = float.MaxValue;
            int count = Physics2D.OverlapCircleNonAlloc(Center, interactRadius, _overlap);
            for (int i = 0; i < count; i++)
            {
                var col = _overlap[i];
                if (col == null) continue;
                var it = col.GetComponentInParent<WorldInteractable>();
                if (it == null || !it.Available(this)) continue;
                // Priority first (gate > key > lantern > mechanism > milk);
                // distance only breaks ties inside one priority band.
                float score = it.Priority * 1000f + Vector2.Distance(Center, it.transform.position);
                if (score < bestScore) { bestScore = score; best = it; }
            }
            _prompt = best;
        }

        void TryInteract()
        {
            if (Dead || _state == MoveState.Hurt) return;
            if (_prompt != null && _prompt.Available(this)) _prompt.Activate(this);
        }

        public void LockFor(float seconds)
        {
            _lockTimer = Mathf.Max(_lockTimer, seconds);
            _state = MoveState.Busy;
            _rb.linearVelocity = Vector2.zero;
        }

        // ---- damage -----------------------------------------------------------

        public void Hurt(int amount, bool lethal, int fromFacing = 0)
        {
            if (Dead) return;
            if (!lethal && _invuln > 0f) return;
            if (lethal) { Die(); return; }

            Health = Mathf.Max(0, Health - amount);
            _invuln = invulnTime;
            _lockTimer = hurtLock;
            int dir = fromFacing != 0 ? fromFacing : -_facing;
            _rb.gravityScale = fallGravity;
            _rb.linearVelocity = new Vector2(dir * hurtKnockback.x, hurtKnockback.y);
            _state = MoveState.Hurt;
            _chain = null;
            HitStop.Punch(0.06f);
            GameEvents.RaisePlayerDamaged(Health, false);
            if (Health <= 0) Die();
        }

        public void Heal(int amount)
        {
            if (Dead) return;
            Health = Mathf.Clamp(Health + amount, 0, maxHealth);
        }

        public void Die()
        {
            if (Dead) return;
            Health = 0;
            _state = MoveState.Dead;
            BladeActive = false;
            _chain = null;
            _rb.linearVelocity = Vector2.zero;
            _rb.gravityScale = fallGravity;
            GameEvents.RaisePlayerDamaged(0, true);
            if (GameDirector.Instance != null) GameDirector.Instance.OnPlayerDied(this);
        }

        public void Freeze()
        {
            _rb.linearVelocity = Vector2.zero;
            if (!Dead) _state = MoveState.Busy;
        }

        public void Respawn(Vector3 position, int health)
        {
            transform.position = position;
            Health = Mathf.Clamp(health, 1, maxHealth);
            _state = MoveState.Idle;
            _chain = null;
            _attackTimer = 0f;
            _lockTimer = 0f;
            _invuln = invulnTime;
            _fallSpeed = 0f;
            BladeActive = false;
            _rb.linearVelocity = Vector2.zero;
            _rb.gravityScale = fallGravity;
            SetFacing(1);
        }

        void SetFacing(int dir) => _facing = dir >= 0 ? 1 : -1;

        void OnDrawGizmosSelected()
        {
            Gizmos.color = new Color(0.4f, 1f, 0.6f, 0.7f);
            Gizmos.DrawWireCube(transform.position + Vector3.up * (bodySize.y * 0.5f), bodySize);
            Gizmos.color = new Color(1f, 0.8f, 0.3f, 0.7f);
            Gizmos.DrawWireCube(transform.position + new Vector3(bladeForward, bladeHeight, 0f), bladeSize);
        }
    }
}
