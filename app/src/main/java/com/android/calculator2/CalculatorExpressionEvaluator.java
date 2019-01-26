/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calculator2;

import com.bkav.calculator2.R;
import com.xlythe.math.NanSyntaxException;
import com.xlythe.math.Solver;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;

public class CalculatorExpressionEvaluator {

    /**
     * The maximum number of significant digits to display.
     */
    private static final int MAX_DIGITS = 17; // Bkav AnhBM: truoc la 12

    /**
     * A {@link Double} has at least 17 significant digits, we show the first {@link #MAX_DIGITS}
     * and use the remaining digits as guard digits to hide floating point precision errors.
     */
    private static final int ROUNDING_DIGITS = Math.max( MAX_DIGITS - 17, 0);

    private final Symbols mSymbols;

    private final CalculatorExpressionTokenizer mTokenizer;

    public CalculatorExpressionEvaluator(CalculatorExpressionTokenizer tokenizer) {
        mSymbols = new Symbols();
        mSolver = new Solver();
        mTokenizer = tokenizer;
    }

    public void evaluate(CharSequence expr, EvaluateCallback callback) {
        evaluate(expr.toString(), callback);
    }

    public void evaluate(String expr, EvaluateCallback callback) {
        expr = mTokenizer.getNormalizedExpression(expr);

        // remove any trailing operators
        while (expr.length() > 0 && "+-/*".indexOf(expr.charAt(expr.length() - 1)) != -1) {
            expr = expr.substring(0, expr.length() - 1);
        }

        try {
            if (expr.length() == 0 || Double.valueOf(expr) != null) {
                callback.onEvaluate(expr, null, Calculator.INVALID_RES_ID);
                return;
            }
        } catch (NumberFormatException e) {
            // expr is not a simple number
        }

        try {
            // AnhBM: dung thu vien tinh toan khac. Neu NAN thi se throw NanSyntaxException
//            String result = mSolver.solve(expr);
//            result = mTokenizer.getLocalizedExpression(doubleToString(Double.parseDouble(result), MAX_DIGITS, ROUNDING_DIGITS));
//            callback.onEvaluate(expr, result, Calculator.INVALID_RES_ID);

            double result = mSymbols.eval(expr);
            if (Double.isNaN(result)) {
                callback.onEvaluate(expr, null, R.string.error_nan);
            } else {
                // The arity library uses floating point arithmetic when evaluating the expression
                // leading to precision errors in the result. The method doubleToString hides these
                // errors; rounding the result by dropping N digits of precision.
                final String resultString = mTokenizer.getLocalizedExpression(
                        doubleToString(callback.isLandscape(), result, MAX_DIGITS, ROUNDING_DIGITS));
                callback.onEvaluate(expr, resultString, Calculator.INVALID_RES_ID);
            }
        } catch (SyntaxException e) {
            // Bkav QuangLH: xu ly NAN
            if (e instanceof NanSyntaxException) {
                callback.onEvaluate(expr, null, R.string.error_nan);
            } else {
                callback.onEvaluate(expr, null, R.string.error_syntax);
            }
        }
    }

    public interface EvaluateCallback {
        public void onEvaluate(String expr, String result, int errorResourceId);

        public boolean isLandscape(); // Bkav QuangLH
    }

    /********************* Bkav **********************/
    private final Solver mSolver;

    public Solver getSolver() {
        return mSolver;
    }

    public static String doubleToString(boolean isLandscape, double x, int maxLenngth, int rounding) {
        return sizeTruncate(doubleToString(isLandscape, x, rounding), maxLenngth);
    }


    private static String doubleToString(boolean isLandscape, double v, int roundingDigits) {
        double absv = Math.abs(v);
        String str = roundingDigits == -1?Float.toString((float)absv):Double.toString(absv);
        StringBuffer buf = new StringBuffer(str);
        int roundingStart = roundingDigits > 0 && roundingDigits <= 13?16 - roundingDigits:17;
        int ePos = str.lastIndexOf(69);
        int exp = ePos != -1?Integer.parseInt(str.substring(ePos + 1)):0;
        if(ePos != -1) {
            buf.setLength(ePos);
        }

        int len = buf.length();

        int dotPos;
        for(dotPos = 0; dotPos < len && buf.charAt(dotPos) != 46; ++dotPos) {
            ;
        }

        exp += dotPos;
        if(dotPos < len) {
            buf.deleteCharAt(dotPos);
            --len;
        }

        int tail;
        for(tail = 0; tail < len && buf.charAt(tail) == 48; ++tail) {
            ++roundingStart;
        }

        if(roundingStart < len) {
            if(buf.charAt(roundingStart) >= 53) {
                for(tail = roundingStart - 1; tail >= 0 && buf.charAt(tail) == 57; --tail) {
                    buf.setCharAt(tail, '0');
                }

                if(tail >= 0) {
                    buf.setCharAt(tail, (char)(buf.charAt(tail) + 1));
                } else {
                    buf.insert(0, '1');
                    ++roundingStart;
                    ++exp;
                }
            }

            buf.setLength(roundingStart);
        }

        if(exp >= -5 && isLandscape ? exp <= 39 : exp <= 14 ) {
            for(tail = len; tail < exp; ++tail) {
                buf.append('0');
            }

            for(tail = exp; tail <= 0; ++tail) {
                buf.insert(0, '0');
            }

            buf.insert(exp <= 0?1:exp, '.');
            exp = 0;
        } else {
            buf.insert(1, '.');
            --exp;
        }

        len = buf.length();

        for(tail = len - 1; tail >= 0 && buf.charAt(tail) == 48; --tail) {
            buf.deleteCharAt(tail);
        }

        if(tail >= 0 && buf.charAt(tail) == 46) {
            buf.deleteCharAt(tail);
        }

        if(exp != 0) {
            buf.append('E').append(exp);
        }

        if(v < 0.0D) {
            buf.insert(0, '-');
        }

        return buf.toString();
    }

    static String sizeTruncate(String str, int maxLen) {
        if(maxLen == 100) {
            return str;
        } else {
            int ePos = str.lastIndexOf(69);
            String tail = ePos != -1?str.substring(ePos):"";
            int tailLen = tail.length();
            int headLen = str.length() - tailLen;
            int maxHeadLen = maxLen - tailLen;
            int keepLen = Math.min(headLen, maxHeadLen);
            if(keepLen < 1 || keepLen < 2 && str.length() > 0 && str.charAt(0) == 45) {
                return str;
            } else {
                int dotPos = str.indexOf(46);
                if(dotPos == -1) {
                    dotPos = headLen;
                }

                if(dotPos > keepLen) {
                    int exponent = ePos != -1?Integer.parseInt(str.substring(ePos + 1)):0;
                    int start = str.charAt(0) == 45?1:0;
                    exponent += dotPos - start - 1;
                    String newStr = str.substring(0, start + 1) + '.' + str.substring(start + 1, headLen) + 'E' + exponent;
                    return sizeTruncate(newStr, maxLen);
                } else {
                    return str.substring(0, keepLen) + tail;
                }
            }
        }
    }
}
