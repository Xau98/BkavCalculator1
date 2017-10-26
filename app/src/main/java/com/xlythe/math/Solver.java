package com.xlythe.math;

import java.util.Locale;

import org.javia.arity.Complex;
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

/**
 * Solves math problems
 * <p>
 * Supports:
 * Basic math + functions (trig, pi)
 * Matrices
 * Hex and Bin conversion
 */
public class Solver {
    // Used for solving basic math
    private static final Symbols sSymbols = new Symbols();
    private BaseModule mBaseModule;
    private int mLineLength = 8;

    public Solver() {
        mBaseModule = new BaseModule(this);
    }

    public static boolean equal(String a, String b) {
        return clean(a).equals(clean(b));
    }

    public static String clean(String equation) {
        return equation
                .replace('-', Constants.MINUS)
                .replace('/', Constants.DIV)
                .replace('*', Constants.MUL)
                .replace(Constants.INFINITY, Constants.INFINITY_UNICODE);
    }

    public static boolean isOperator(char c) {
        return ("" +
                Constants.PLUS +
                Constants.MINUS +
                Constants.DIV +
                Constants.MUL +
                Constants.POWER).indexOf(c) != -1;
    }

    public static boolean isOperator(String c) {
        return isOperator(c.charAt(0));
    }

    public static boolean isNegative(String number) {
        return number.startsWith(String.valueOf(Constants.MINUS)) || number.startsWith("-");
    }

    public static boolean isDigit(char number) {
        return String.valueOf(number).matches(Constants.REGEX_NUMBER);
    }

    /**
     * Input an equation as a string
     * ex: sin(150)
     * and get the result returned.
     */
    public String solve(String input) throws SyntaxException {
        if (input.trim().isEmpty()) {
            return "";
        }

        // Drop final operators (they can only result in error)
        int size = input.length();
        while (size > 0 && isOperator(input.charAt(size - 1))) {
            input = input.substring(0, size - 1);
            --size;
        }

        // Convert to decimal
        String decimalInput = convertToDecimal(input);

        Complex value = sSymbols.evalComplex(decimalInput);

        String real = "";
        for (int precision = mLineLength; precision > 6; precision--) {
            real = tryFormattingWithPrecision(value.re, precision);
            if (real.length() <= mLineLength) {
                break;
            }
        }

        String imaginary = "";
        for (int precision = mLineLength; precision > 6; precision--) {
            imaginary = tryFormattingWithPrecision(value.im, precision);
            if (imaginary.length() <= mLineLength) {
                break;
            }
        }

        real = clean(mBaseModule.changeBase(real, Base.DECIMAL, mBaseModule.getBase()));
        imaginary = clean(mBaseModule.changeBase(imaginary, Base.DECIMAL, mBaseModule.getBase()));

        String result = "";
        if (value.re != 0 && value.im == 1) result = real + "+" + "i";
        else if (value.re != 0 && value.im > 0) result = real + "+" + imaginary + "i";
        else if (value.re != 0 && value.im == -1) result = real + "-" + "i";
        else if (value.re != 0 && value.im < 0) result = real + imaginary + "i"; // Implicit -
        else if (value.re != 0 && value.im == 0) result = real;
        else if (value.re == 0 && value.im == 1) result = "i";
        else if (value.re == 0 && value.im == -1) result = "-i";
        else if (value.re == 0 && value.im != 0) result = imaginary + "i";
        else if (value.re == 0 && value.im == 0) result = "0";

        return result.trim();
    }

    public double eval(String input) throws SyntaxException {
        return sSymbols.eval(input);
    }

    public void pushFrame() {
        sSymbols.pushFrame();
    }

    public void popFrame() {
        sSymbols.popFrame();
    }

    public void define(String var, double val) {
        sSymbols.define(var, val);
    }

    public String convertToDecimal(String input) throws SyntaxException {
        return mBaseModule.changeBase(input, mBaseModule.getBase(), Base.DECIMAL);
    }

    String tryFormattingWithPrecision(double value, int precision) throws SyntaxException {
        if (Double.isNaN(value)) {
            // Bkav QuangLH:
            throw new NanSyntaxException();
            //throw new SyntaxException();
        }

        // The standard scientific formatter is basically what we need. We will
        // start with what it produces and then massage it a bit.
        String result = String.format(Locale.US, "%" + mLineLength + "." + precision + "g", value);
        String mantissa = result;
        String exponent = null;
        int e = result.indexOf('e');
        if (e != -1) {
            mantissa = result.substring(0, e);

            // Strip "+" and unnecessary 0's from the exponent
            exponent = result.substring(e + 1);
            if (exponent.startsWith("+")) {
                exponent = exponent.substring(1);
            }
            exponent = String.valueOf(Integer.parseInt(exponent));
        }

        int period = mantissa.indexOf('.');
        if (period == -1) {
            period = mantissa.indexOf(',');
        }
        if (period != -1) {
            // Strip trailing 0's
            while (mantissa.length() > 0 && mantissa.endsWith("0")) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
            if (mantissa.length() == period + 1) {
                mantissa = mantissa.substring(0, mantissa.length() - 1);
            }
        }

        if (exponent != null) {
            result = mantissa + 'e' + exponent;
        } else {
            result = mantissa;
        }
        return result;
    }

    public void setLineLength(int length) {
        mLineLength = length;
    }

    public Base getBase() {
        return mBaseModule.getBase();
    }

    public void setBase(Base base) {
        mBaseModule.setBase(base);
    }

    public BaseModule getBaseModule() {
        return mBaseModule;
    }

    public Symbols getSymbols() {
        return sSymbols;
    }
}
