package com.android.liujun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class CarDiagnosticsActivity extends Activity {
    /** Called when the activity is first created. */
	private static final boolean JAY_DEBUG = true;
	private static final String TAG = "CarDiagnoticsActivity";

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        
        //生成动态数组，并且转入数据
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
//        for(int i=0;i<10;i++)
//        {
//        	HashMap<String, Object> map = new HashMap<String, Object>();
//        	map.put("ItemImage", R.drawable.temperature);//添加图像资源的ID
//			map.put("ItemText", "NO."+String.valueOf(i));//按序号做ItemText
//        	lstImageItem.add(map);
//        }
    	HashMap<String, Object> map1 = new HashMap<String, Object>();
    	map1.put("ItemImage", R.drawable.check);//添加图像资源的ID
		map1.put("ItemIndex", 1);//按序号做ItemText
    	lstImageItem.add(map1);
    	HashMap<String, Object> map2 = new HashMap<String, Object>();
    	map2.put("ItemImage", R.drawable.temperature);//添加图像资源的ID
		map2.put("ItemIndex", 2);//按序号做ItemText
    	lstImageItem.add(map2);
    	HashMap<String, Object> map3 = new HashMap<String, Object>();
    	map3.put("ItemImage", R.drawable.speed);//添加图像资源的ID
		map3.put("ItemIndex", 3);//按序号做ItemText
    	lstImageItem.add(map3);
    	
    	
        //生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
        SimpleAdapter saImageItems = new SimpleAdapter(this, //没什么解释
        		                                    lstImageItem,//数据来源 
        		                                    R.layout.item,//night_item的XML实现
        		                                    
        		                                    //动态数组与ImageItem对应的子项        
        		                                    new String[] {"ItemImage"}, 
        		                                    
        		                                    //ImageItem的XML文件里面的一个ImageView,两个TextView ID
        		                                    new int[] {R.id.ItemImage}
        );
        //添加并且显示
        gridview.setAdapter(saImageItems);
        //添加消息处理
        gridview.setOnItemClickListener(new ItemClickListener());
    }
    
  //当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
    class  ItemClickListener implements OnItemClickListener
    {
    	public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened 
    			                          View arg1,//The view within the AdapterView that was clicked
    			                          int arg2,//The position of the view in the adapter
    			                          long arg3//The row id of the item that was clicked
    			                          ) {
    		//在本例中arg2=arg3
    		HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);
    		//显示所选Item的ItemText
    		int index = (Integer) item.get("ItemIndex");
    		switch (index) {
    		case 2:
    	    	Intent mIntent = new Intent();
    	    	mIntent.setClass(CarDiagnosticsActivity.this, Temperature.class);
    	    	startActivity(mIntent);
    			break;
    		case 3:
    			Intent intentSpeed = new Intent();
    			intentSpeed.setClass(CarDiagnosticsActivity.this, CarSpeed.class);
    			startActivity(intentSpeed);
    			break;

    		default:
    			break;
    		}
    	}
    }  
	
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		super.onResume();
    	if(JAY_DEBUG) {
    		Log.e(TAG, "**on resume**");
    	}

	}
	

	
	@Override
	protected void onDestroy() {

		// TODO Auto-generated method stub
		super.onDestroy();
    	if(JAY_DEBUG) {
    		Log.e(TAG, "**on destory**");
    	}
	}
    
}
