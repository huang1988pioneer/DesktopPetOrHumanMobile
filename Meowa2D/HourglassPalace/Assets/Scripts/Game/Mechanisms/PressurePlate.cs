using System.Collections.Generic;
using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Weight-triggered plate.  A jackal is heavy enough too, which is half the
    /// point: a guard chasing you across the hall can open the door for you.
    /// </summary>
    public sealed class PressurePlate : MonoBehaviour, ISnapshotable
    {
        public List<MonoBehaviour> targets = new List<MonoBehaviour>();
        [Tooltip("Once pressed, stays pressed - used for permanent route changes.")]
        public bool latching;
        public bool enemiesCanPress = true;
        public float sinkDepth = 0.12f;
        public float releaseDelay = 0.25f;
        public Transform plateVisual;

        Vector3 _up;
        bool _pressed;
        bool _latched;
        float _release;
        int _occupants;
        string _snapshotId;

        public string SnapshotId => _snapshotId;

        void Awake()
        {
            if (plateVisual == null) plateVisual = transform;
            _up = plateVisual.position;
            _snapshotId = HierarchyId.Of(transform);
        }

        void Start() => Push(false);

        void Update()
        {
            if (_release > 0f)
            {
                _release -= Time.deltaTime;
                if (_release <= 0f && _occupants <= 0 && !_latched) Push(false);
            }
            var target = _pressed ? _up + Vector3.down * sinkDepth : _up;
            plateVisual.position = Vector3.MoveTowards(plateVisual.position, target, 1.2f * Time.deltaTime);
        }

        void OnTriggerEnter2D(Collider2D other)
        {
            if (!Weighty(other)) return;
            _occupants++;
            _release = 0f;
            Push(true);
            if (latching) _latched = true;
        }

        void OnTriggerExit2D(Collider2D other)
        {
            if (!Weighty(other)) return;
            _occupants = Mathf.Max(0, _occupants - 1);
            if (_occupants == 0 && !_latched) _release = releaseDelay;
        }

        bool Weighty(Collider2D other)
        {
            if (other.GetComponentInParent<PlayerController>() != null) return true;
            return enemiesCanPress && other.GetComponentInParent<EnemyBase>() != null;
        }

        void Push(bool pressed)
        {
            _pressed = pressed;
            for (int i = 0; i < targets.Count; i++)
                if (targets[i] is IMechanismTarget m) m.SetPowered(pressed);
        }

        public object Capture() => _latched;

        public void Restore(object state)
        {
            _latched = state is bool latched && latched;
            _occupants = 0;
            _release = 0f;
            Push(_latched);
            plateVisual.position = _latched ? _up + Vector3.down * sinkDepth : _up;
        }
    }
}
