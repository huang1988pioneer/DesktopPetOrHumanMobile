namespace HourglassPalace
{
    public interface IDamageable
    {
        bool Downed { get; }
        void TakeHit(int amount, int fromFacing, bool lethal);
    }
}
