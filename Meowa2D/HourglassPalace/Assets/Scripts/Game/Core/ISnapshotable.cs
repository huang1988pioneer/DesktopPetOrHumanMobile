namespace HourglassPalace
{
    /// <summary>
    /// Anything a lantern remembers.  Capture runs when a lantern is lit,
    /// Restore when the cat respawns at it, so a mechanism thrown before the
    /// lantern stays thrown and a guard felled after it comes back.
    /// </summary>
    public interface ISnapshotable
    {
        string SnapshotId { get; }
        object Capture();
        void Restore(object state);
    }
}
