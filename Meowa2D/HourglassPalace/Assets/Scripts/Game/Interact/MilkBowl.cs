using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// A bowl of milk: two paws back, once, and it stays empty for the rest of
    /// the run even across deaths.  Drinking plants the cat for a moment, so it
    /// is a decision about safety and not a reflex during a fight.
    /// </summary>
    public sealed class MilkBowl : WorldInteractable
    {
        public int healAmount = 2;
        public float drinkTime = 0.6f;
        public SpriteRenderer bowl;

        bool _empty;

        public override int Priority => PriorityMilk;
        public override PromptIcon Icon => PromptIcon.Paw;
        public override string Label => "喝牛奶";

        public override bool Available(PlayerController player)
        {
            if (!isActiveAndEnabled || _empty || player == null) return false;
            // Not mid-flinch, not mid-air: you have to stand still to drink.
            return player.CanUseConsumable && player.Health < player.maxHealth;
        }

        public override void Activate(PlayerController player)
        {
            if (_empty) return;
            _empty = true;
            player.LockFor(drinkTime);
            player.Heal(healAmount);
            if (bowl != null) bowl.color = new Color(0.55f, 0.5f, 0.48f, 0.85f);
        }
    }
}
