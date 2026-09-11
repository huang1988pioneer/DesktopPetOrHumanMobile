using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// One logical screen of the Hall of Falling Sands.  The camera never shows
    /// anything outside the room the cat is standing in, so nothing unexplored
    /// is spoiled and every screen can pose its own single question.
    /// </summary>
    public sealed class LevelRoom : MonoBehaviour
    {
        public int index = 1;
        public string screenName = "";
        public Rect bounds = new Rect(0f, 0f, 20f, 11.25f);

        public bool Contains(Vector2 point) => bounds.Contains(point);

        public Vector2 Center => bounds.center;

        void OnDrawGizmos()
        {
            Gizmos.color = new Color(0.4f, 0.8f, 1f, 0.35f);
            Gizmos.DrawWireCube(bounds.center, new Vector3(bounds.width, bounds.height, 0.1f));
        }
    }
}
