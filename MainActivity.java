package com.flk.convertunit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText mInputEdt;
    //单位选择下拉框
    private Spinner mInputSpinner;
    private Spinner mOutputSpinner;

    private TextView mResultTv;

    //可选择的单位列表
    private List<Unit> mCDList = initCDList();
    private List<Unit> mZLList = initZLList();
    private List<Unit> mWDList = initWDList();

    //输入的单位类型
    private int mInputCDType = 1;
    private int mInputZLType = 2;
    private int mInputWDType = 3;

    //当前选中的单位输入类型
    private int mCurSelectedType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化view
        mInputEdt = findViewById(R.id.edt_input);
        mInputSpinner = findViewById(R.id.sp_input);
        mOutputSpinner = findViewById(R.id.sp_output);
        mResultTv = findViewById(R.id.tv_result);
        //初始化输入下拉框选择单位列表
        List<Unit> inputList = new ArrayList<>();
        inputList.addAll(mCDList);
        inputList.addAll(mZLList);
        inputList.addAll(mWDList);
        //初始化输入下拉框选择列表
        ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, covList(inputList));
        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInputSpinner.setAdapter(inputAdapter);
        //输入下拉框选中变化监听
        mInputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = getTypeFromInputPos(position);
                if (mCurSelectedType == pos) {
                    return;
                }
                showOutData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //监听转换按钮点击
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mInputEdt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(v.getContext(), "please enter conversion value", Toast.LENGTH_SHORT).show();
                    return;
                }
                float data = Float.parseFloat(text);
                int position = mInputSpinner.getSelectedItemPosition();
                Unit in = inputList.get(position);
                Unit out = getListForType(mCurSelectedType).get(mOutputSpinner.getSelectedItemPosition());
                double result = calData(data, in.step, out.step);
                //防止显示成科学计数法，只保留8位小数
                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(8);
                mResultTv.append("\n" + text + in.name + "=" + nf.format(result) + out.name);
            }
        });

        mInputSpinner.setSelection(0);
    }

    /**
     * 温度单位转换
     * @param data 输入值
     * @param src 输入单位
     * @param tar 目标单位
     * @return 目标单位值
     */
    private double calWD(float data, float src, float tar) {
        if (src == 1) {
            data = (data - 32) / 1.8f;
        } else if (src == 2) {
            data = data - 273.15f;
        }
        if (tar == 1) {
            return (data * 1.8f) + 32;
        } else if (tar == 2) {
            return data + 273.15f;
        }
        return data;
    }

    /**
     * 单位转换
     * @param data 输入值
     * @param src 输入单位
     * @param tar 目标单位
     * @return 目标单位值
     */
    private double calData(float data, float src, float tar) {
        if (mCurSelectedType == mInputWDType) {
            return calWD(data, src, tar);
        }
        double base = data * src;
        return base / tar;
    }

    /**
     * 根据选中下标获取单位类型
     * @param position 输入下拉框选中下标
     * @return 单位类型
     */
    private int getTypeFromInputPos(int position) {
        if (position < mCDList.size()) {
            return mInputCDType;
        } else if (position < mCDList.size() + mZLList.size()) {
            return mInputZLType;
        } else {
            return mInputWDType;
        }
    }

    /**
     * 根据单位类型返回当前单位可选择的单位列表
     * @param type 输入单位类型
     * @return 对应的单位列表
     */
    private List<Unit> getListForType(int type) {
        if (type == mInputCDType) {
            return mCDList;
        }
        if (type == mInputZLType) {
            return mZLList;
        }
        return mWDList;
    }

    /**
     * 根据输入选中下标显示不用的单位列表
     * @param position 输入单位选中的下标
     */
    private void showOutData(int position) {
        mCurSelectedType = getTypeFromInputPos(position);
        ArrayAdapter<String> outAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, covList(getListForType(mCurSelectedType)));
        outAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOutputSpinner.setAdapter(outAdapter);
    }

    /**
     * 将单位列表转换成下拉框显示的列表
     * @param list 单位列表
     * @return 下拉框显示的列表
     */
    private List<String> covList(List<Unit> list) {
        List<String> r = new ArrayList<>();
        for (Unit t : list) {
            r.add(t.name);
        }
        return r;
    }

    /**
     * 长度用乘法换算，换算时先转换成厘米，在转换为目标单位
     * @return 长度可选择的单位列表
     */
    private static List<Unit> initCDList() {
        List<Unit> list = new ArrayList<>();
        list.add(Unit.create("inch", 2.54f));
        list.add(Unit.create("foot", 30.48f));
        list.add(Unit.create("ard", 91.44f));
        list.add(Unit.create("mile", 160934f));
        return list;
    }

    /**
     * 重量用乘法换算，换算时先转换成千克，在转换为目标单位
     * @return 重量可选择的单位列表
     */
    private static List<Unit> initZLList() {
        List<Unit> list = new ArrayList<>();
        list.add(Unit.create("pound ", 0.453592f));
        list.add(Unit.create("ounce", 0.0283495f));
        list.add(Unit.create("ton", 907.185f));
        return list;
    }

    /**
     * 温度不适合单用乘法转换，在转换时根据{@link Unit#step}类型自行转换
     * @return 温度可选择的单位列表
     */
    private static List<Unit> initWDList() {
        List<Unit> list = new ArrayList<>();
        list.add(Unit.create("Celsius(°C)", 0));
        list.add(Unit.create("Fahrenheit(°F)", 1));
        list.add(Unit.create("Kelvin(°K)", 2));
        return list;
    }
}