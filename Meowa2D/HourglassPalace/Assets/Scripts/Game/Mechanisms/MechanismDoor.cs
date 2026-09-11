using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// A gate or a retracting slab.  Closed it blocks a route and nothing more -
    /// a shut door never damages the cat, it only takes a path away.
    /// </summary>
    public sealed class MechanismDoor : MonoBehaviour, IMechanismTarget, ISnapshotable
    {
        [Tooltip("Where the door sits when open, relative to its closed position.")]
        public Vector2 openOffset = new Vector2(0f, 3.6f);
        public float moveSpeed = 6f;
        [Tooltip("Tick for a platform that is present until something powers it off.")]
        public bool openWhenUnpowered;
        public bool startOpen;

        Vector3 _closed;
        Vector3 _open;
        bool _powered;
        string _snapshotId;

        public string SnapshotId => _snapshotId;
        public bool Open => openWhenUnpowered ? !_powered : _powered;

        void Awake()
        {
            _closed = transform.position;
            _open = _closed + (Vector3)openOffset;
            _powered = startOpen != openWhenUnpowered;
            transform.position = Open ? _open : _closed;
            _snapshotId = HierarchyId.Of(transform);
        }

        void Update()
        {
            var target = Open ? _open : _closed;
            if ((transform.position - target).sqrMagnitude > 0.0001f)
                transform.position = Vector3.MoveTowards(transform.position, target, moveSpeed * Time.deltaTime);
        }

        public void SetPowered(bool powered) => _powered = powered;

        public object Capture() => _powered;

        public void Restore(object state)
        {
            if (state is bool powered) _powered = powered;
            transform.position = Open ? _open : _closed;
        }
    }
}
