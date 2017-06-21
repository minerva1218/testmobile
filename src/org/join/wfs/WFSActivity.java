package org.join.wfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.join.wfs.server.WebService;
import org.join.wfs.util.CopyUtil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class WFSActivity extends Activity implements OnCheckedChangeListener {

	private ToggleButton toggleBtn;
	private TextView urlText,textView1,textView2;
	private Button fin;
	private ImageView qrCode;
	private Intent intent;
	private File file;
	private String path,dirin,dirout;
	private String info;
	private String formInput;
	private int ih,iw,oh,ow,num=0;
	private Animation am,bm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initViews();
		initFiles();
		/////////////////////clean/////////////////////
		
		/*fin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File SdCard = Environment.getExternalStorageDirectory();
				 File fileDir = new File( SdCard.getPath()+"/Pictures/SMP" );
				  File[] fileList = fileDir.listFiles();
				  {for(int k=0; k<fileList.length; k++){fileList[k].delete();}}
				  	finish();
			}
		});*/
		//////////////////////////////////////////////
		//////////////////some thing//////////////////
		 am = new TranslateAnimation(0,0,-1000,0);
		        am.setDuration( 2000 );
		        am.setRepeatCount( 0 );
		 bm = new TranslateAnimation(0,0,2000,0);
		        bm.setDuration( 2000 );
		        bm.setRepeatCount( 0 );
		 textView1.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha));
		 textView2.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha));
		 

		//////////////////////////////////////////////
		

		intent = new Intent(this, WebService.class);
		///////////////////creat file///////////////////
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			File sdFile = android.os.Environment.getExternalStorageDirectory();
			String FILE_PATH = "/Pictures/";
			String pathf = sdFile.getPath()+FILE_PATH+ File.separator + "SMP";
			File dirFile = new File(pathf);
			  
		if(!dirFile.exists()){dirFile.mkdir();}}
			dirin = //Environment.getExternalStorageDirectory().getPath()+ "/DCIM/100ANDRO/";
					Environment.getExternalStoragePublicDirectory
			(Environment.DIRECTORY_DCIM).toString()+"/100ANDRO";
		file = new File( dirin );
		
		//////////////////////////////////////////
	}

	private void initViews() {
		toggleBtn = (ToggleButton) findViewById(R.id.toggleBtnn);
		toggleBtn.setOnCheckedChangeListener(this);
		urlText = (TextView) findViewById(R.id.urlText);
		qrCode = (ImageView) findViewById(R.id.img01);
		textView1 = (TextView)findViewById(R.id.textView1);
		textView2 = (TextView)findViewById(R.id.textView2);
		//fin = (Button)findViewById(R.id.fin);
	}

	private void initFiles() {
		new CopyUtil(this).assetsCopy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			
			String ip = getLocalIpAddress();
			if (ip == null) {
				Toast.makeText(this, R.string.msg_net_off, Toast.LENGTH_SHORT)
						.show();
				urlText.setText("");
			} else {
				startService(intent);
				urlText.setText("http://" + ip + ":" + WebService.PORT + "/");
				urlText.setAnimation(bm);
				bm.startNow();
				String sip="http://" + ip + ":" + WebService.PORT + "/";
				Bitmap image = null;
				try {
					if (sip != null && !"".equals(sip)) 
					{image = createTwoQRCode(sip);
					BrowserFile( file );}
				    } 
				catch (Exception e) {
					e.printStackTrace();
				}
				if (image != null) 
				{	qrCode.setImageBitmap(image);
					qrCode.setAnimation(am);
					am.startNow();
					}
			
			}
		} else {
			stopService(intent);
			urlText.setText("");
			qrCode.setImageBitmap(null);
			
			File SdCard = Environment.getExternalStorageDirectory();
			 File fileDir = new File( SdCard.getPath()+"/DCIM/100ANDRO/SMP" );
			  File[] fileList = fileDir.listFiles();
			  {for(int k=0; k<fileList.length; k++){fileList[k].delete();}}
		}
	}
	
	/** QRcode產生器 */
	public Bitmap createTwoQRCode(String content) throws Exception {
		DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        int vWidth = dm.widthPixels;
        int vHeight = dm.heightPixels;
        int dmout;
		if(vWidth>vHeight)
        	 dmout = vHeight;
		else
			 dmout = vWidth;
		BitMatrix matrix = new MultiFormatWriter().encode(content,
				BarcodeFormat.QR_CODE, dmout*3/2, dmout*3/2);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = 0xff000000;
				}
			}
		}
		//SHOWIMAGE
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bm.setPixels(pixels, 0, width, 0, 0, width, height);
		return bm;
	}

	/** 獲取當前IP地址 */
	public String getLocalIpAddress() {
		try {
			// 網路接口
			Enumeration<NetworkInterface> infos = NetworkInterface
					.getNetworkInterfaces();
			while (infos.hasMoreElements()) {
				// 獲取網路接口
				NetworkInterface niFace = infos.nextElement();
				Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
				while (enumIpAddr.hasMoreElements()) {
					InetAddress mInetAddress = enumIpAddr.nextElement();
					// IP地址不是127.0.0.1時返回得得到的IP
					if (!mInetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(mInetAddress
									.getHostAddress())) {
						return mInetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {

		}
		return null;
	}


	@Override
	public void onBackPressed() {
		if (intent != null) {
			stopService(intent);
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		File SdCard = Environment.getExternalStorageDirectory();
		 File fileDir = new File( SdCard.getPath()+"/DCIM/100ANDRO/SMP" );
		  File[] fileList = fileDir.listFiles();
		  {for(int k=0; k<fileList.length; k++){fileList[k].delete();}}
		stopService(intent);
		System.exit(0);
	}

	/**縮圖處理並放入資料夾*/
	public void BrowserFile(File file) {
		ToSearchFiles(file);
	}
	public void ToSearchFiles(File file) {
		File[] the_Files = file.listFiles() ;
		for (File tempF : the_Files) {
			if (tempF.isDirectory()) {
				ToSearchFiles(tempF);
			} else
			{
				try {
					if (tempF.getName().indexOf(".jpg") > -1||tempF.getName().indexOf(".JPG") > -1) {
						path = tempF.getPath();
						Uri uri = Uri.parse("file://"+path);
						BitmapFactory.Options option = new BitmapFactory.Options();
						option.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(uri.getPath(),option);
						iw = option.outWidth;ih = option.outHeight;
						ow = 160; oh = 90;
						int scale = Math.min(iw/ow,ih/oh);
						option.inJustDecodeBounds = false;
						option.inSampleSize = scale;
						Bitmap bmp = BitmapFactory.decodeFile(uri.getPath(),option);
						try {
							String fileName =  Uri . parse (path). getLastPathSegment (); 
							File fileout = new File( Environment.getExternalStorageDirectory()
    							.getAbsolutePath()+"/DCIM/100ANDRO/"+"SMP", fileName);
							FileOutputStream out = new FileOutputStream(fileout );
							bmp.compress ( Bitmap. CompressFormat.JPEG , 90 , out);
							out.flush ();
							out.close ();
						} catch (FileNotFoundException e) {
							e.printStackTrace ();
						} catch (IOException e) {
							e.printStackTrace ();
						}}} catch (Exception e) {}}}}}