using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// The moon key.  One use, one lock: it exists only to be put into the moon
    /// gate.  Hidden until the djinn falls, and hidden again if the run is
    /// rolled back to a lantern from before that fight.
    /// </summary>
    public sealed class MoonKeyItem : WorldInteractable, ISnapshotable
    {
        public float bobAmplitude = 0.12f;
        public float bobRate = 2.1f;

        Vector3 _home;
        bool _taken;
        string _snapshotId;

        public override int Priority => PriorityKey;
        public override PromptIcon Icon => PromptIcon.Moon;
        public override string Label => "拾取月鑰";
        public string SnapshotId => _snapshotId;

        void Awake()
        {
            _home = transform.position;
            _snapshotId = HierarchyId.Of(transform);
        }

        void Update()
        {
            transform.position = _home + Vector3.up * (Mathf.Sin(Time.time * bobRate) * bobAmplitude);
        }

        public override bool Available(PlayerController player) => isActiveAndEnabled && !_taken;

        public override void Activate(PlayerController player)
        {
            if (_taken) return;
            _taken = true;
            if (GameDirector.Instance != null) GameDirector.Instance.GiveMoonKey();
            gameObject.SetActive(false);
        }

        sealed class Snap
        {
            public bool Taken;
            public bool Revealed;
        }

        public object Capture() => new Snap { Taken = _taken, Revealed = gameObject.activeSelf || _taken };

        public void Restore(object state)
        {
            if (state is not Snap snap) return;
            _taken = snap.Taken;
            transform.position = _home;
            gameObject.SetActive(snap.Revealed && !snap.Taken);
        }
    }
}
