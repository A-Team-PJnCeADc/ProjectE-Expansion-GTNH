package com.projecte.expansion.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * 紧凑的 EMC 数字格式化,移植自上游 Project Expansion 的 EMCFormat。
 * 一百万及以上的数值使用短级差后缀(M、B、T、Qa……No),好让 BigInteger 量级的数值(可达约 1e60)
 * 在提示框与 GUI 中保持可读。
 *
 * <p>
 * 版式与上游一致:有效组 + "." + 下一组的前两位数字,例如 12345678 -&gt; "12.34M"。
 */
public final class EMCFormatPX {

    public static final BigDecimal FORMAT_START = BigDecimal.valueOf(1_000_000L);

    private EMCFormatPX() {}

    public static String format(long value) {
        return format(BigInteger.valueOf(value));
    }

    public static String format(BigInteger value) {
        return format(new BigDecimal(value));
    }

    public static String format(BigDecimal value) {
        if (value.abs()
            .compareTo(FORMAT_START) < 0) {
            return NumberFormat.getNumberInstance(Locale.US)
                .format(value.toBigInteger());
        }
        NumberName name = NumberName.findName(value);
        if (name == null) {
            return NumberFormat.getNumberInstance(Locale.US)
                .format(value.toBigInteger());
        }
        return getString(value.toBigInteger()) + name.getName(true);
    }

    /** "12345678" -&gt; "12.34"(上游的有效组/十进位组对)。 */
    static String getString(BigInteger value) {
        String str = value.toString();
        ArrayList<String> groups = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = str.length(); i > 0; i--) {
            if (current.length() >= 3) {
                groups.add(
                    current.reverse()
                        .toString());
                current.delete(0, current.length());
            }
            current.append(str.charAt(i - 1));
        }
        if (current.length() > 0) {
            groups.add(
                current.reverse()
                    .toString());
        }
        String significant = groups.get(groups.size() - 1);
        String decimals = groups.size() > 1 ? groups.get(groups.size() - 2)
            .substring(
                0,
                Math.min(
                    2,
                    groups.get(groups.size() - 2)
                        .length()))
            : "00";
        return String.format("%s.%s", significant, decimals);
    }

    public enum NumberName {

        MILLION(1e6, "M"),
        BILLION(1e9, "B"),
        TRILLION(1e12, "T"),
        QUADRILLION(1e15, "Qa"),
        QUINTILLION(1e18, "Qi"),
        SEXTILLION(1e21, "Sx"),
        SEPTILLION(1e24, "Sp"),
        OCTILLION(1e27, "O"),
        NONILLION(1e30, "N"),
        DECILLION(1e33, "D"),
        UNDECILLION(1e36, "U"),
        DUODECILLION(1e39, "Du"),
        TREDECILLION(1e42, "Tr"),
        QUATTUORDECILLION(1e45, "Qt"),
        QUINDECILLION(1e48, "Qd"),
        SEXDECILLION(1e51, "Sd"),
        SEPTENDECILLION(1e54, "St"),
        OCTODECILLION(1e57, "Oc"),
        NOVEMDECILLION(1e60, "No");

        public static final NumberName[] VALUES = values();

        private final double value;
        private final String shortName;

        NumberName(double val, String shortName) {
            this.value = val;
            this.shortName = shortName;
        }

        public String getName() {
            return getName(false);
        }

        public String getName(boolean getShort) {
            return getShort ? shortName
                : (name().charAt(0) + name().toLowerCase(Locale.US)
                    .substring(1)).trim();
        }

        public double getValue() {
            return value;
        }

        public BigDecimal getBigDecimalValue() {
            return new BigDecimal(String.valueOf(value));
        }

        static NumberName findName(BigDecimal value) {
            return Arrays.stream(VALUES)
                .filter(v -> value.compareTo(v.getBigDecimalValue()) > -1)
                .reduce((first, second) -> second)
                .orElse(null);
        }
    }
}
