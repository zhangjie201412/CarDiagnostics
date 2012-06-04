package com.android.liujun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;


public class CarSpeed extends Activity{
    /** Called when the activity is first created. */

	private boolean running = true;
	//for bluetooth
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private String address = "00:19:5D:24:E5:4A";
	//private String address = "84:00:D2:CA:35:A5";
	private BluetoothDevice mBluetoothDevice = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;
	private static final String TAG = "Temperature";
	private static final boolean JAY_DEBUG = true;
	private int retry = 10;
	private TextView TVSpeed = null;
	private indicateView iView = null;
	
	private static final int warningSpeed = 200;
	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speed);
        mContext = this;
        
        TVSpeed = (TextView)findViewById(R.id.TVSpeed);
        iView = (indicateView) findViewById(R.id.indicate);

		BTSetup();
		new Thread(new MyThread()).start();
    }
    
	@Override
	protected void onStop() {
		if (JAY_DEBUG) {
			Log.e(TAG, "**on stop**");
		}
		// TODO Auto-generated method stub
		super.onStop();
		running = false;
	}
	
	@Override
	protected void onResume() {
		if (JAY_DEBUG) {
			Log.e(TAG, "**on resume**");
		}
		// TODO Auto-generated method stub
		super.onStop();
		running = true;
	}
	
	@Override
	protected void onStart() {
		if (JAY_DEBUG) {
			Log.e(TAG, "**on start**");
		}
		// TODO Auto-generated method stub
		super.onStop();
		running = true;
	}
	
	@Override
	protected void onDestroy() {
		if (JAY_DEBUG) {
			Log.e(TAG, "**on destory**");
		}
		// TODO Auto-generated method stub
		super.onDestroy();
		running = false;
		if(btSocket != null)
		{
			try {
				btSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
    
    
	public void BTSetup()
	{
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
        	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG);
        	finish();
        	return ;
        }
        if(!mBluetoothAdapter.isEnabled()) {
        	Toast.makeText(this, "Please enable your bluetooth and re-run this program!", Toast.LENGTH_LONG);
        	finish();
        	return ;
        }
        
        if(JAY_DEBUG) {
        	Log.d(TAG, "***goet local bluetooth adapter!*");
        }
        
    	mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);

    	try {
    		btSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
    	} catch (IOException e) {
			// TODO: handle exception
    		Log.e(TAG, "ON RESUME: socket failed create!", e);
		}
    	
    	try {
			btSocket.connect();
			Log.d(TAG, "ON RESUME: BT connection established, data transfer link open!");
		} catch (IOException e) {
			// TODO: handle exception
			try {
				Log.d(TAG, "ON RESUME: connect error then close!!", e);
				btSocket.close();
			} catch (IOException e2) {
				// TODO: handle exception
				Log.e(TAG, "ON RESUME: Unable to close socket during connection failure", e2);
			}
		}
	}
	
	int end;
	Handler indicateHandler = new Handler() {
		public void handleMessage(Message msg) {
			// //////////////////////
			super.handleMessage(msg);
			iView.setValue(msg.what);
			iView.invalidate();
		}
	};

	public class indicateThread implements Runnable {
		public void run() {
			int value = iView.getVaule();
			if (end > 270)
				end = 270;
			// /////
			while (value != end) {
				try {
					if (value < end) {
						value += 1;
					} else {
						value -= 1;
					}
					Thread.sleep(15);
					Message message = new Message();
					message.what = value;
					indicateHandler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// //////////////////////
			// int value = msg.what;
			super.handleMessage(msg);
			int carSpeed;
			Bundle bundle = msg.getData();
			carSpeed = bundle.getInt("CarSpeed");

			TVSpeed.setText("Temperature ==> "+ carSpeed);
			
			if(carSpeed > warningSpeed) {
		        //play a voice
		        MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.sound);
		        mPlayer.start();
			}
			
			end = carSpeed - 90;
			new Thread(new indicateThread()).start();
			// iView.setValue(360);
			// iView.invalidate();
		}
	};

	public class MyThread implements Runnable {

		public void run() {
			byte[] buffer;
			byte[] readBuffer = new byte[64];
			int bytes = 0;
			System.out.println("go to thread!");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			while (running) {
				try {
					outStream = btSocket.getOutputStream();
					inStream = btSocket.getInputStream();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Log.e(TAG, "ON RESUME: Input stream creation failed.", e1);
				}

				int CarSpeed = 0;
				String msgStr;
				// /////////////car speed/////////////////

				msgStr = "010D";
				buffer = msgStr.getBytes();
				retry = 0;
				bytes = 0;
				while (bytes != 17) {
					retry++;
					if (retry > 10) {
						System.out.println("ERROR: CarSpeed retry timeout!");
						break;
					}
					try {
						outStream.write(buffer);
						outStream.write(0x0d);
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
					try {
						bytes = inStream.read(readBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (readBuffer[bytes - 1] != 62) {
					System.out
							.println("CAR SPEED READ ERROR: Last byte is not < ");
				}
				if (JAY_DEBUG) {
					for (int i = 0; i < bytes; i++) {
						System.out.println("=> buff[" + i + "] = "
								+ readBuffer[i]);
					}
					System.out.println("temperature " + readBuffer[11] + " "
							+ readBuffer[12]);
				}
				if (readBuffer[11] > 63)
					readBuffer[11] -= 7;
				if (readBuffer[12] > 63)
					readBuffer[12] -= 7;
				CarSpeed = (readBuffer[11] - 48) * 16 + readBuffer[12] - 48;
				if (JAY_DEBUG) {
					System.out.println("read car speed: " + CarSpeed);
				}
				// ////////////////////////////////////////

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("CarSpeed", CarSpeed);
				message.setData(bundle);
				handler.sendMessage(message);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// }
		}
	}
}
