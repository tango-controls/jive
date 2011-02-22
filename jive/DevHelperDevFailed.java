package jive;

/****************************************************************************
 *
 * $Revision$
 *
 */

final public class DevHelperDevFailed extends Exception
{
    public
    DevHelperDevFailed()
    {
    }

    public
    DevHelperDevFailed(String _ob_a3,
             String _ob_a4,
             String _ob_a5)
    {
        code   = 0;
        reason = _ob_a3;
        origin = _ob_a4;
        desc   = _ob_a5;
    }

    public int code;
    public String reason;
    public String origin;
    public String desc;
}
