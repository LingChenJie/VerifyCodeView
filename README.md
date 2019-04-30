# VerifyCodeView
自定义方形验证码输入框

简介见：https://blog.csdn.net/linglingchenchen/article/details/89686384

使用如下
```xml
<com.jc.verifycode.VerifyCodeEditText
            android:id="@+id/verifyCode"
            android:layout_marginTop="60dp"
            android:layout_marginStart="20dp"
            android:layout_marginEnd="20dp"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:textColor="@color/verifycode_select_color"
            android:inputType="number"
            android:textSize="18sp"
            app:codeMargin="20dp"
            app:borderRadius="6dp"
            app:borderWidth="2dp"
            app:cursorColor="@color/verifycode_select_color"
            app:selectBorderColor="@color/verifycode_select_color"
            app:normalBorderColor="@color/verifycode_normal_color"
            app:cursorWidth="1dp"/>
            
    
    
    <!--验证码的属性-->
    <declare-styleable name="VerifyCodeEditText">
        <attr name="figures" format="integer"/><!--验证码的个数-->
        <attr name="codeMargin" format="dimension"/><!--验证码的之间的间隔-->
        <attr name="selectBorderColor" format="reference"/><!--选中的边框颜色-->
        <attr name="normalBorderColor" format="reference"/><!--普通的边框颜色-->
        <attr name="borderRadius" format="dimension"/><!--边角的滑度 -->
        <attr name="borderWidth" format="dimension"/><!--边框的厚度 -->
        <attr name="cursorDuration" format="integer"/><!--光标跳动的间隔时间 -->
        <attr name="cursorWidth" format="dimension"/><!--光标的宽度 -->
        <attr name="cursorColor" format="color|reference"/><!--光标的颜色 -->
    </declare-styleable>
```
