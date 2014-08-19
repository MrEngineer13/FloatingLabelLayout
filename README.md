# Floating Label Edit Text Pattern for Android

# Features

1. Ability to edit EditText/FloatingLabelLayout in the layout
2. Can handle orientation changes
3. Customize view styling 
4. Two triggers, one based on focus and the other based on the input
5. Backwards compatible to 2.3.x

![Float label design pattern](http://dribbble.s3.amazonaws.com/users/6410/screenshots/1254439/form-animation-_gif_.gif)
  

# Usage
1. Add *floatinglabel* module as a dependency to your project

2a. Either add the following to your xml:

        <com.mrengineer13.fll.FloatingLabelEditText
            xmlns:fll="http://schemas.android.com/apk/res-auto"
            android:id="@+id/fll_username"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            fll:floatLabelTrigger="text"
            fll:floatLabelTextAppearance="@style/TextAppearance.FLL.FloatLabel">
    
            <EditText
                android:id="@+id/edit_username"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="@string/account_username_hint"
                android:singleLine="true"
                android:inputType="textNoSuggestions"
                android:imeOptions="actionNext"
                android:nextFocusDown="@+id/edit_password" />
    
        </com.mrengineer13.fll.FloatingLabelEditText>
        
2b. or if you want to create one programmatically then use

     FloatingLabelEditText floatingLabelEditText = new FloatingLabelEditText(Activity.this);
        

# Known Bugs

## 1. onFocusChangeListener bug

As of v0.1 this library uses onFocusChangeListener so @Overriding it for the EditText will not work. Try add a [TextWatcher](http://developer.android.com/reference/android/text/TextWatcher.html) instead.

# Credit:

1. [Matt Smith](http://mattdsmith.com/float-label-pattern/)  and [Google](http://www.google.com/design/spec/components/text-fields.html#text-fields-floating-labels) for the idea
2. [Chris Banes](https://gist.github.com/chrisbanes/11247418) for his implementation

<!-- ## Developers-->

<!-- 1. [MrEngineer](https://github.com/MrEngineer13) -->