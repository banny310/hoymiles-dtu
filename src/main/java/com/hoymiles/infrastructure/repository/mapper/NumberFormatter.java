package com.hoymiles.infrastructure.repository.mapper;

import jakarta.enterprise.context.Dependent;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Dependent
@NoArgsConstructor
public class NumberFormatter {

    /**
     * Round number to given precision
     *
     * @param number
     * @param precision
     * @return
     */
    public static float round(float number, int precision) {
        double f = Math.pow(10, precision);
        return (float) (Math.round(number * f) / f);
    }

    /**
     * PHP like number format
     *
     * @param value
     * @param decimals
     * @param dec_point
     * @param thousands_sep
     * @return
     */
    public static @NotNull String numberFormat(float value, int decimals, String dec_point, String thousands_sep) {
        int prec = Math.abs(decimals);
        String text = (prec > 0 ? String.valueOf(round(value, prec)) : String.valueOf(Math.round(value)));
        List<String> s = Arrays.asList(text.split("\\."));
        while (s.size() < 1) {
            s.add("");
        }

        if (s.get(0).length() > 3) {
            String tmp = s.get(0).replaceAll("\\B(?=(?:\\d{3})+(?!\\d))", thousands_sep);
            s.set(0, tmp);
        }
        if (s.size() > 1) {
            while (s.get(1).length() < prec) {
                s.set(1, s.get(1) + '0');
            }
        }
        return join(dec_point, s);//s.get(0) + dec_point + s.get(1);
    }

    public static @NotNull <T extends CharSequence> String join(@NonNull CharSequence delimiter, @NonNull Iterable<T> tokens) {
        final Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
        return sb.toString();
    }

    /**
     * @param value
     * @param decimals
     * @return
     */
    public static @NotNull String numberFormat(float value, int decimals) {
        return numberFormat(value, decimals, ",", " ");
    }

    public float format0fd(float number) {
        return Math.round(number);
    }

    public float format1fd(float number) {
        return round(number, 1);
    }

    public float format3fd(float number) {
        return round(number, 3);
    }
}
