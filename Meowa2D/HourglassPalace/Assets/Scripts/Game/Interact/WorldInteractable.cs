namespace HourglassPalace
{
    using UnityEngine;

    /// <summary>
    /// Anything E acts on.  Priority resolves overlaps in the order the design
    /// fixes - moon gate, moon key, lantern, mechanism, milk - so two things in
    /// arm's reach never make the button ambiguous.
    /// </summary>
    public abstract class WorldInteractable : MonoBehaviour
    {
        public const int PriorityGate = 0;
        public const int PriorityKey = 1;
        public const int PriorityLantern = 2;
        public const int PriorityMechanism = 3;
        public const int PriorityMilk = 4;

        public abstract int Priority { get; }
        public abstract PromptIcon Icon { get; }
        public abstract string Label { get; }

        public virtual bool Available(PlayerController player) => isActiveAndEnabled;

        public abstract void Activate(PlayerController player);
    }
}
