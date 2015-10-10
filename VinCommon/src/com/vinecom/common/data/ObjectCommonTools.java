package com.vinecom.common.data;

/**
 * Created by ndn on 7/21/2015.
 */
final class ObjectCommonTools {
    private ObjectCommonTools() {

    }

    static Integer getInteger(Object o) {
        Long l = getLong(o);
        if (l != null) {
            return l.intValue();
        }
        return null;
    }

    static Long getLong(Object o) {
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Byte) {
            return (long) (Byte) o;
        }
        if (o instanceof Short) {
            return (long) (Short) o;
        }
        if (o instanceof Character) {
            return (long) (Character) o;
        }
        if (o instanceof Float) {
            return (long) (float) o;
        }
        if (o instanceof Double) {
            return (long) (double) o;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1l : 0l;
        }
        if (o instanceof Integer) {
            return (long) (Integer) o;
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    static Short getShort(Object o) {
        Long l = getLong(o);
        if (l != null) {
            return l.shortValue();
        }
        return null;
    }

    static Byte getByte(Object o) {
        Long l = getLong(o);
        if (l != null) {
            return l.byteValue();
        }
        return null;
    }

    static Character getChar(Object o) {
        if (o instanceof Character) {
            return (Character) o;
        } else {
            return null;
        }
    }

    static Double getDouble(Object o) {
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof Float) {
            return (double) (Float) o;
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    static Float getFloat(Object o) {
        if (o instanceof Double) {
            return (float) (double) o;
        }
        if (o instanceof Float) {
            return (Float) o;
        }

        try {
            return Float.parseFloat(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    static Boolean getBool(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        try {
            return Boolean.parseBoolean(o.toString());
        } catch (Exception e) {
            return null;
        }
    }


}
