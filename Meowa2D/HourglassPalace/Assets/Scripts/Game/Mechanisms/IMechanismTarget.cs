namespace HourglassPalace
{
    /// <summary>
    /// Anything a plate, a lever or the djinn can switch.  Powered is simply
    /// "the mechanism is being driven"; each target decides what that means -
    /// a door opens, a spike bed stalls, a platform goes out.
    /// </summary>
    public interface IMechanismTarget
    {
        void SetPowered(bool powered);
    }
}
