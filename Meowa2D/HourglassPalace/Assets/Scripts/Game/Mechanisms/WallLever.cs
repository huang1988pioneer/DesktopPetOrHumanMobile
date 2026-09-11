using System.Collections.Generic;
using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Bronze wall lever.  Pulling it plants the cat for the duration - that
    /// commitment is the cost.  A timed lever locks the route open only for a
    /// while, which is what makes the order you pull them matter.
    /// </summary>
    public sealed class WallLever : WorldInteractable, ISnapshotable
    {
        public List<MonoBehaviour> targets = new List<MonoBehaviour>();
        public float pullTime = 0.5f;
        [Tooltip("0 keeps it thrown for good; anything else is a countdown door.")]
        public float holdSeconds;
        public Transform handle;
        public Vector3 pulledHandleOffset = new Vector3(0f, -0.22f, 0f);

        Vector3 _handleUp;
        bool _thrown;
        float _timer;
        string _snapshotId;

        public override int Priority => PriorityMechanism;
        public override PromptIcon Icon => PromptIcon.Hand;
        public override string Label => _thrown && holdSeconds > 0f
            ? $"拉桿  {Mathf.CeilToInt(_timer)}"
            : "拉桿";

        public string SnapshotId => _snapshotId;
        public bool Thrown => _thrown;

        void Awake()
        {
            if (handle == null) handle = transform;
            _handleUp = handle.localPosition;
            _snapshotId = HierarchyId.Of(transform);
        }

        void Start() => Drive(false);

        void Update()
        {
            if (!_thrown || holdSeconds <= 0f) return;
            if (GameDirector.Paused) return;
            _timer -= Time.deltaTime;
            if (_timer <= 0f) SetThrown(false);
        }

        public override bool Available(PlayerController player)
        {
            if (!isActiveAndEnabled) return false;
            if (holdSeconds <= 0f && _thrown) return false;   // already locked open
            return player != null && player.Grounded;
        }

        public override void Activate(PlayerController player)
        {
            player.LockFor(pullTime);
            SetThrown(true);
        }

        void SetThrown(bool thrown)
        {
            _thrown = thrown;
            _timer = thrown ? holdSeconds : 0f;
            handle.localPosition = thrown ? _handleUp + pulledHandleOffset : _handleUp;
            Drive(thrown);
        }

        void Drive(bool powered)
        {
            for (int i = 0; i < targets.Count; i++)
                if (targets[i] is IMechanismTarget m) m.SetPowered(powered);
        }

        public object Capture() => holdSeconds > 0f ? false : _thrown;

        public void Restore(object state) => SetThrown(state is bool thrown && thrown);
    }
}
