using System.Text;
using UnityEngine;

namespace HourglassPalace
{
    public static class HierarchyId
    {
        /// <summary>Stable id for snapshot dictionaries: the object's scene path.</summary>
        public static string Of(Transform t)
        {
            var sb = new StringBuilder(t.name);
            var p = t.parent;
            while (p != null)
            {
                sb.Insert(0, '/').Insert(0, p.name);
                p = p.parent;
            }
            return sb.ToString();
        }
    }
}
