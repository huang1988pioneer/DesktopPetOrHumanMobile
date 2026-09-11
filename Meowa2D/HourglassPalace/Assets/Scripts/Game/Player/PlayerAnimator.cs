using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Drives Miaobaibai's sprite from the controller's state.  Only the idle
    /// and walk loops exist as generated art, so the remaining states are posed
    /// from those frames and carried by code-driven tilt, squash and tint -
    /// readable now, and a drop-in swap when the rest of the sheet is produced.
    /// </summary>
    [RequireComponent(typeof(SpriteRenderer))]
    public sealed class PlayerAnimator : MonoBehaviour
    {
        public PlayerController player;
        public Sprite[] idleFrames;
        public Sprite[] walkFrames;
        public SpriteRenderer bladeFlash;

        public float idleFps = 8f;
        public float walkFps = 10f;
        public float runFps = 16f;
        public float climbFps = 5f;

        SpriteRenderer _sr;
        Color _base;
        float _clock;
        PlayerController.MoveState _last;

        void Awake()
        {
            _sr = GetComponent<SpriteRenderer>();
            _base = _sr.color;
            if (player == null) player = GetComponentInParent<PlayerController>();
            if (bladeFlash != null) bladeFlash.enabled = false;
        }

        void LateUpdate()
        {
            if (player == null) return;
            var state = player.State;
            if (state != _last) { _clock = 0f; _last = state; }
            _clock += Time.deltaTime;

            _sr.flipX = player.Facing < 0;
            var tilt = Vector3.zero;
            var scale = Vector3.one;
            var tint = _base;

            switch (state)
            {
                case PlayerController.MoveState.Idle:
                    _sr.sprite = Frame(idleFrames, idleFps);
                    break;
                case PlayerController.MoveState.Walk:
                    _sr.sprite = Frame(walkFrames, walkFps);
                    break;
                case PlayerController.MoveState.Run:
                    _sr.sprite = Frame(walkFrames, runFps);
                    tilt = new Vector3(0f, 0f, -4f * player.Facing);
                    break;
                case PlayerController.MoveState.Rise:
                    _sr.sprite = Pose(walkFrames, 2);
                    scale = new Vector3(0.94f, 1.07f, 1f);
                    break;
                case PlayerController.MoveState.Fall:
                    _sr.sprite = Pose(walkFrames, 6);
                    scale = new Vector3(1.05f, 0.95f, 1f);
                    break;
                case PlayerController.MoveState.Hang:
                    _sr.sprite = Pose(idleFrames, 0);
                    scale = new Vector3(0.92f, 1.04f, 1f);
                    break;
                case PlayerController.MoveState.ClimbUp:
                    _sr.sprite = Pose(walkFrames, 1);
                    break;
                case PlayerController.MoveState.Chain:
                    _sr.sprite = Frame(walkFrames, climbFps, 2);
                    scale = new Vector3(0.88f, 1.06f, 1f);
                    break;
                case PlayerController.MoveState.Attack:
                    _sr.sprite = player.BladeActive ? Pose(walkFrames, 5) : Pose(walkFrames, 3);
                    tilt = new Vector3(0f, 0f, (player.BladeActive ? -7f : 4f) * player.Facing);
                    break;
                case PlayerController.MoveState.Hurt:
                    _sr.sprite = Pose(idleFrames, 4);
                    tint = new Color(1f, 0.42f, 0.36f, 1f);
                    break;
                case PlayerController.MoveState.Dead:
                    _sr.sprite = Pose(idleFrames, 0);
                    tilt = new Vector3(0f, 0f, -82f * player.Facing);
                    tint = new Color(0.72f, 0.66f, 0.7f, 1f);
                    break;
                default:
                    _sr.sprite = Frame(idleFrames, idleFps);
                    break;
            }

            // Invulnerability blinks rather than tinting, so the hurt red stays
            // meaningful as "that one landed".
            if (player.Invulnerable && state != PlayerController.MoveState.Dead)
                tint.a = Mathf.Repeat(Time.unscaledTime, 0.16f) < 0.08f ? 0.35f : 1f;

            _sr.color = tint;
            transform.localEulerAngles = tilt;
            transform.localScale = new Vector3(scale.x, scale.y, 1f);

            if (bladeFlash != null)
            {
                bool show = player.BladeActive;
                bladeFlash.enabled = show;
                if (show)
                {
                    bladeFlash.flipX = player.Facing < 0;
                    var p = bladeFlash.transform.localPosition;
                    p.x = Mathf.Abs(p.x) * player.Facing;
                    bladeFlash.transform.localPosition = p;
                }
            }
        }

        Sprite Frame(Sprite[] frames, float fps, int stride = 1)
        {
            if (frames == null || frames.Length == 0) return _sr.sprite;
            int i = Mathf.FloorToInt(_clock * fps) * stride;
            return frames[((i % frames.Length) + frames.Length) % frames.Length];
        }

        Sprite Pose(Sprite[] frames, int index)
        {
            if (frames == null || frames.Length == 0) return _sr.sprite;
            return frames[Mathf.Clamp(index, 0, frames.Length - 1)];
        }
    }
}
