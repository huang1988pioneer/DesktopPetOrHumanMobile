using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Follows the cat but obeys the room: the view is clamped to the current
    /// screen's bounds, and changing room eases across instead of cutting.
    /// </summary>
    [RequireComponent(typeof(Camera))]
    public sealed class CameraDirector : MonoBehaviour
    {
        public Transform target;
        public Vector2 offset = new Vector2(0f, 1.3f);
        public float followSmoothing = 0.12f;
        public float roomChangeSmoothing = 0.3f;

        Camera _cam;
        LevelRoom[] _rooms;
        LevelRoom _room;
        Vector3 _velocity;
        float _smoothing;

        public LevelRoom CurrentRoom => _room;

        void Awake()
        {
            _cam = GetComponent<Camera>();
            _smoothing = followSmoothing;
        }

        void Start()
        {
            _rooms = FindObjectsByType<LevelRoom>(FindObjectsSortMode.None);
            if (target != null)
            {
                _room = RoomAt(target.position);
                transform.position = Desired(target.position);
            }
        }

        void LateUpdate()
        {
            if (target == null) return;
            var next = RoomAt(target.position);
            if (next != null && next != _room)
            {
                _room = next;
                _smoothing = roomChangeSmoothing;
            }
            else if (_smoothing > followSmoothing)
                _smoothing = Mathf.MoveTowards(_smoothing, followSmoothing, Time.deltaTime * 0.6f);

            transform.position = Vector3.SmoothDamp(transform.position, Desired(target.position),
                ref _velocity, _smoothing);
        }

        LevelRoom RoomAt(Vector2 point)
        {
            if (_rooms == null) return _room;
            if (_room != null && _room.Contains(point)) return _room;
            for (int i = 0; i < _rooms.Length; i++)
                if (_rooms[i] != null && _rooms[i].Contains(point)) return _rooms[i];
            return _room;
        }

        Vector3 Desired(Vector3 focus)
        {
            var p = new Vector3(focus.x + offset.x, focus.y + offset.y, -10f);
            if (_room == null) return p;

            float halfH = _cam.orthographicSize;
            float halfW = halfH * _cam.aspect;
            var b = _room.bounds;

            p.x = b.width <= halfW * 2f
                ? b.center.x
                : Mathf.Clamp(p.x, b.xMin + halfW, b.xMax - halfW);
            p.y = b.height <= halfH * 2f
                ? b.center.y
                : Mathf.Clamp(p.y, b.yMin + halfH, b.yMax - halfH);
            return p;
        }
    }
}
