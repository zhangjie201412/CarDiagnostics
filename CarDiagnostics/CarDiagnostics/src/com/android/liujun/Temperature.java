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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;


public class Temperature extends Activity implements Callback{
    /** Called when the activity is first created. */
	private SurfaceView mSurface;
	private SurfaceHolder mHolder;
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
	private TextView TVTemp = null;

	private int x,y;
	private static final int warningTemp = 200;
	private Context mContext;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature);
        mContext = this;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        x = dm.widthPixels;
        y = dm.heightPixels;
        
//        System.out.println("x: "+x+" y: "+y);
        
        TVTemp = (TextView)findViewById(R.id.TVTemp);
//        
		mSurface = (SurfaceView) findViewById(R.id.surface);

		mHolder = mSurface.getHolder();
		mHolder.addCallback(this);
		
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
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// //////////////////////
			// int value = msg.what;
			super.handleMessage(msg);
			int temp;
			Bundle bundle = msg.getData();
			temp = bundle.getInt("Temp");
			
			TVTemp.setText("Temperature ==> "+ temp);
			//+++++if temperature is too hiigh warn
			
			if(temp > warningTemp) {
		        //play a voice
		        MediaPlayer mPlayer = MediaPlayer.create(mContext, R.raw.sound);
		        mPlayer.start();
			}
			draw(temp);
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

				int temp = 0;
				String msgStr;
				// /////////////temp/////////////////

				msgStr = "0105";
				buffer = msgStr.getBytes();
				retry = 0;

				while (bytes != 17) {
					retry++;
					if (retry > 10) {
						System.out.println("ERROR: Temperature retry timeout!");
						break;
					}
					try {
						outStream.write(buffer);
						outStream.write(0x0d);
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
					try {
						System.out.println("3");
						bytes = inStream.read(readBuffer);
						System.out.println("4");
						for (int i = 0; i < bytes; i++) {
							System.out.println("=> buff[" + i + "] = "
									+ readBuffer[i]);
						}
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
					System.out.println("READ ERROR: Last byte is not < ");
				}
				if (JAY_DEBUG) {
					for (int i = 0; i < bytes; i++) {
						System.out.println("=> buff[" + i + "] = "
								+ readBuffer[i]);
					}
					System.out.println("temperature " + readBuffer[11] + " "
							+ readBuffer[12]);
				}
				temp = (readBuffer[11] - 48) * 16 + readBuffer[12] - 48;
				if (JAY_DEBUG) {
					System.out.println("read temperature: " + temp);
				}

				// ////////////////////////////////////////

				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("Temp", temp);
				message.setData(bundle);
				handler.sendMessage(message);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	private void draw(int temp) {
		int yy = (int) (140-temp/2);
		Canvas canvas = mHolder.lockCanvas();
		Paint mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		canvas.drawRect(x/2-15, 30, x/2+15, y-130, mPaint);
		Paint mPaint1 = new Paint();
		mPaint1.setColor(Color.RED);

		canvas.drawRect(x/2-15, yy, x/2+15, y-130, mPaint1);
		
		Paint mPaint2 = new Paint();
		mPaint2.setColor(Color.RED);
		canvas.drawCircle(x/2, y-120, 25, mPaint2);
		
		Paint paintLine = new Paint();
		paintLine.setColor(Color.BLUE);
		
		int ydegree = 160;
		int tem = -40;
		while (ydegree > 40) {
			canvas.drawLine(x/2+15, ydegree, x/2+22, ydegree, mPaint);
			if (ydegree % 20 == 0) {
				canvas.drawLine(x/2+15, ydegree, x/2+22, ydegree, paintLine);
				canvas.drawText(tem + "", x/2+25, ydegree + 4, mPaint);
				tem += 40;
			}
			ydegree = ydegree - 2;
		}
		mHolder.unlockCanvasAndPost(canvas);// 更新屏幕显示内容
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}
}
