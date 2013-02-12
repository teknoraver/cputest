package net.teknoraver.cputest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.AssetManager;

public class CpuTest extends ListActivity implements Runnable {
	private static final String CPUINFO = "/proc/cpuinfo";
	private StringBuilder email = new StringBuilder();
	private ArrayList<Test> tests = new ArrayList<Test>(); 
	private Handler handler = new Handler();

	class Test implements Runnable
	{
		String name;
		boolean result;

		public Test(String n, boolean r) {
			name = n;
			result = r;
		}

		@Override
		public String toString()
		{
			return name + ": " +  result + "\n";
		}

		@Override
		public void run() {
			if(name != null) {
				tests.add(this);
				((TestAdapter)getListAdapter()).notifyDataSetChanged();
			} else
				findViewById(R.id.send).setEnabled(true);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cpu_test);

		setListAdapter(new TestAdapter(this, tests));

		new Thread(this).start();
	}

	private boolean runCommand(String file) throws IOException, InterruptedException {
		System.out.println("running: " + file);
		Process test = Runtime.getRuntime().exec(getFilesDir() + "/" + file);
		test.waitFor();
		return test.exitValue() == 0;
	}

	private void getFile(String name) throws IOException {
		AssetManager am = getAssets();
		System.out.println("extracting: " + name);
                final File file = new File(getFilesDir(), name);
                if(file.exists())
                	return;
                getFilesDir().mkdirs();
                final InputStream in = am.open(name);
                final FileOutputStream out = new FileOutputStream(file);
                final byte[] buf = new byte[65536];
                int len;
                while((len = in.read(buf)) > 0)
                        out.write(buf, 0, len);
                in.close();
                out.close();
                Runtime.getRuntime().exec(new String[]{"chmod", "755", file.getPath()});
	}

	@Override
	public void run() {
		AssetManager am = getAssets();
		try {
			for(String file : am.list("")) {
				if(file.equals("images") ||
				   file.equals("sounds") ||
				   file.equals("webkit"))
					continue;
				try {
					getFile(file);
					String testName = file;
					boolean testResult = runCommand(file);
					Test test = new Test(testName, testResult);
					email.append(test);
					handler.post(test);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.post(new Test(null, false));
	}

	public void sendMail(View v) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {getString(R.string.email)});
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_sub) + ": " + Build.MANUFACTURER + " " + Build.MODEL);
		intent.putExtra(Intent.EXTRA_TEXT, email.toString());
		File file = new File(CPUINFO);
		if(file.exists() && file.canRead())
			intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + CPUINFO));
		startActivity(Intent.createChooser(intent, "send email"));
	}
}

class TestAdapter extends ArrayAdapter<CpuTest.Test>
{
	public TestAdapter(CpuTest context, ArrayList<CpuTest.Test> tests) {
		super(context, 0, tests);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView name = (TextView)convertView;
		if(name == null)
			name = new TextView(getContext());

		CpuTest.Test test = getItem(position);

		name.setText(test.name);
		name.setCompoundDrawablesWithIntrinsicBounds(0, 0, test.result ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background, 0);

		return name;
	}
}