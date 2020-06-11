package machine;

import java.util.*;

public class Unpack
{
    /*
     * Look up an SExp of kind SExpKind.ATOM in a map using a provided key, and return the atom's value (a String).
     * If there is no SExp for that key in the map, then an error will be thrown unless 'optional' is true, in which case optionalDefault will be returned instead.
     * If there is an SExp for the key, but it is of the wrong kind (something other than SExpKind.ATOM), then an error will be thrown regardless of 'optional'.
     */
    public static String getAtomItem(Map<String,SExp> map, String key, boolean optional, String optionalDefault)
    {
        SExp exp = getSExpItem(map,key,optional,null);

        if(exp == null)
        {
            return optionalDefault;
        }

        if(exp.getKind() != SExpKind.ATOM)
        {
            throw UnpackError.badField(key);
        }

        return exp.getAtom();
    }

    // Same as getAtomItem, but for booleans (encoded as integers).
    public static boolean getBooleanItem(Map<String,SExp> map, String key, boolean optional, boolean optionalDefault)
    {
        SExp exp = getSExpItem(map,key,optional,null);

        if(exp == null)
        {
            return optionalDefault;
        }

        if(exp.getKind() != SExpKind.INT) // booleans as ints
        {
            throw UnpackError.badField(key);
        }

        return exp.getBoolFromInt();
    }

    // Same as getAtomItem, but for SExpKind.INT.
    public static int getIntItem(Map<String,SExp> map, String key, boolean optional, int optionalDefault)
    {
        SExp exp = getSExpItem(map,key,optional,null);

        if(exp == null)
        {
            return optionalDefault;
        }

        if(exp.getKind() != SExpKind.INT)
        {
            throw UnpackError.badField(key);
        }

        return exp.getInt();
    }

    // Same as getAtomItem, but for SExpKind.LIST.
    public static List<SExp> getListItem(Map<String,SExp> map, String key, boolean optional, List<SExp> optionalDefault)
    {
        SExp exp = getSExpItem(map,key,optional,null);

        if(exp == null)
        {
            return optionalDefault;
        }

        if(exp.getKind() != SExpKind.LIST)
        {
            throw UnpackError.badField(key);
        }

        return exp.getList();
    }

    // Similar to getAtomItem, but looks up any kind of SExp (and returns it rather than extracting a specific value from it).
    public static SExp getSExpItem(Map<String,SExp> map, String key, boolean optional, SExp optionalDefault)
    {
        SExp exp = map.get(key);

        if(exp == null)
        {
            if(optional)
            {
                return optionalDefault;
            }
            else
            {
                throw UnpackError.badField(key);
            }
        }
        else
        {
            return exp;
        }
    }

    // Same as getAtomItem, but for SExpKind.STRING.
    public static String getStringItem(Map<String,SExp> map, String key, boolean optional, String optionalDefault)
    {
        SExp exp = getSExpItem(map,key,optional,null);

        if(exp == null)
        {
            return optionalDefault;
        }

        if(exp.getKind() != SExpKind.STRING)
        {
            throw UnpackError.badField(key);
        }

        return exp.getString();
    }
}
