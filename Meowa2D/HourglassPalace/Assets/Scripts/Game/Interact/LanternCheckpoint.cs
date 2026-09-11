using UnityEngine;
using UnityEngine.Rendering.Universal;

namespace HourglassPalace
{
    /// <summary>
    /// A lantern.  Lighting it records the run - position, paws, mechanisms,
    /// key, which guards are still standing - and becomes the place the cat
    /// steps back in from.  It never refills health: a checkpoint is not a
    /// free clinic.
    /// </summary>
    public sealed class LanternCheckpoint : WorldInteractable, ISnapshotable
    {
        public int index;
        public string screenName = "";
        public SpriteRenderer flame;
        public Light2D glow;
        public Vector3 spawnOffset = new Vector3(0f, 0.1f, 0f);
        public bool litAtStart;

        static readonly Color Lit = new Color(1f, 0.84f, 0.42f);
        static readonly Color Unlit = new Color(0.42f, 0.4f, 0.44f);

        bool _lit;
        string _snapshotId;

        public Vector3 SpawnPoint => transform.position + spawnOffset;
        public bool IsLit => _lit;
        public string SnapshotId => _snapshotId;

        public override int Priority => PriorityLantern;
        public override PromptIcon Icon => PromptIcon.Paw;
        public override string Label => _lit ? "燈籠已亮" : "點亮燈籠";

        void Awake()
        {
            _snapshotId = HierarchyId.Of(transform);
            SetLit(litAtStart);
        }

        void Start()
        {
            if (litAtStart && GameDirector.Instance != null)
                GameDirector.Instance.RegisterLantern(this);
        }

        public override bool Available(PlayerController player) => isActiveAndEnabled && !_lit;

        public override void Activate(PlayerController player)
        {
            if (_lit) return;
            SetLit(true);
            if (GameDirector.Instance != null) GameDirector.Instance.RegisterLantern(this);
        }

        void SetLit(bool lit)
        {
            _lit = lit;
            if (flame != null) flame.color = lit ? Lit : Unlit;
            if (glow != null) glow.intensity = lit ? 1.15f : 0f;
        }

        void Update()
        {
            if (!_lit || flame == null) return;
            // a warm flame that breathes, so a lit lantern reads from a screen away
            float f = 0.88f + Mathf.PerlinNoise(Time.time * 3.1f, index * 7.3f) * 0.22f;
            flame.color = new Color(Lit.r * f, Lit.g * f, Lit.b * f, 1f);
        }

        public object Capture() => _lit;

        public void Restore(object state) => SetLit(state is bool lit && lit);
    }
}
