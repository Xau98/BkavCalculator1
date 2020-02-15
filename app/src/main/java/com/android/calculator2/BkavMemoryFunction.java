package com.android.calculator2;


public class BkavMemoryFunction {
    private  String mMemory;
    private String mInput="";

    boolean isExitMemory(){
        if(!mInput.isEmpty() ){
            return true;
        }
        return false;
    }

     void onClearMemory(){
        mInput="";
     }

     String onRecallMemory(){
        return mInput;
     }

      void onMPlusAddMemory(String input  ){
         if (mInput.equals("")) {
                 mInput = input;
         } else {
             if(input.charAt(0) == KeyMaps.MINUS_SIGN)
                 mInput = mInput  + input;
             else
                 mInput = mInput + "+" + input;
         }

     }

     void onMSubAddMemory(String input){
         // Bkav TienNVh : Xet truong hop bien mINPUT da co noi dung chua
         if (mInput.equals("")) {
                 // Bkav TienNVh : kiem tra input >0 de co the lay duoc input.charAt(0)
                 if (input.charAt(0) == KeyMaps.MINUS_SIGN) {
                     // Bkav TienNVh : Neu nhap vao la -a thi luu vao a (vi --a = a)
                     mInput = input.substring(1);
                 } else {
                     // Bkav TienNVh : Nguoc lai neu nhap vao la a thi luu vao la -a
                     mInput = Character.toString(KeyMaps.MINUS_SIGN) + input;
                 }
         } else {
             // Bkav TienNVh : Xet truong hop : --a= a
             if (input.charAt(0) == KeyMaps.MINUS_SIGN) {
                 // Bkav TienNVh : Chuyen dau - thanh dau + . Bang cach cắt bỏ dấu trừ thêm vào đó là dáu cộng  (--a = +a)
                 mInput = mInput + "+" + input.substring(1);
             } else {
                 mInput = mInput + Character.toString(KeyMaps.MINUS_SIGN) + input;
             }
         }
     }
}
