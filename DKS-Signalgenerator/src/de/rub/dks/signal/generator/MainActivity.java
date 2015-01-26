package de.rub.dks.signal.generator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import de.rub.dks.signal.generator.helper.UpdateHelper;
import de.rub.dks.signal.generator.sound.OneSignal;
import de.rub.dks.signal.generator.sound.RectangularSignal;
import de.rub.dks.signal.generator.sound.SawtoothSignal;
import de.rub.dks.signal.generator.sound.SiSignal;
import de.rub.dks.signal.generator.sound.Signal;
import de.rub.dks.signal.generator.sound.SinusSignal;
import de.rub.dks.signal.generator.sound.TriangularSignal;
import de.rub.dks.signal.generator.sound.ZeroSignal;
import de.rub.dks.signal.generator.sound.arithmetic.ArithmeticSignal;
import de.rub.dks.signal.generator.sound.arithmetic.DifferenceSignal;
import de.rub.dks.signal.generator.sound.arithmetic.DivisionSignal;
import de.rub.dks.signal.generator.sound.arithmetic.MultiplySignal;
import de.rub.dks.signal.generator.sound.arithmetic.SumSignal;

/**
 * 
 * @author Tim Guenther, Max Hoffmann
 * @version 0.0.1
 * @since 01.07.2014
 * 
 */

public class MainActivity extends ActionBarActivity {

	private final String sharedPref = "de.rub.dks.signal.generator.Preferences";
	private final String onFirstSound = "de.rub.dks.signal.generator.onFirstSound";
	private final String agb = "de.rub.dks.signal.generator.AGB";

	private GraphView graphView;
	private GraphView sumGraphView;
	private GraphViewSeries seriesSinStatic;
	private GraphViewSeries seriesSinMod;
	private GraphViewSeries seriesSum;

	// Constants
	private final static double MIN_FREQ = 0, MAX_FREQ = 20 * Math.PI, MIN_PHASE = 0, MAX_PHASE = 2 * Math.PI;
	private static int LAYOUT_SIZE;

	private boolean isRunning = true;
	private Thread t = null;

	private double selectedFreq, selectedFreq1, selectedPhase;
	private boolean actionByCode, typing = false;
	private boolean toggleSignal = false, togglePhase = false, toggleFrequency = false, toggleSound = false;

	private Spinner sig1_spin;
	private Spinner sig2_spin;
	private Spinner op_spin;

	private SeekBar freq_bar;
	private SeekBar phase_bar;
	private SeekBar freq_bar1;

	private EditText freq_txt;
	private EditText phase_txt;
	private EditText freq_txt1;
	
	private Button equalButton;

	private Signal signal1, signal2;
	private ArithmeticSignal arithmeticSignal;

	private GestureDetector gestrureDetector;

	// Round on 2 digits
	private double round(double d) {
		return (int) (d * 100) / 100.0;
	}

	public static void onCoachMark(final Context context) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.coach_mark);
		dialog.setCanceledOnTouchOutside(true);
		// for dismissing anywhere you touch
		View masterView = dialog.findViewById(R.id.coach_mark_master_view);
		masterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	/**
	 * Recalculates the graph-series:
	 * 
	 * Generates new series with new offset in phase and frequency.
	 * 
	 */
	private void recalculateGraphs() {
		GraphViewData[] data = signal2.getGraphData(selectedFreq, selectedPhase);
		seriesSinMod.resetData(data);

		data = signal1.getGraphData(selectedFreq1, 0);
		seriesSinStatic.resetData(data);

		data = arithmeticSignal.getGraphData();
		seriesSum.resetData(data);

		if (!typing) {
			actionByCode = true;
			phase_txt.setText("" + round(selectedPhase / Math.PI));
			freq_txt.setText("" + round(selectedFreq / Math.PI));
			freq_txt1.setText("" + round(selectedFreq1 / Math.PI));
			actionByCode = false;
		}
		typing = false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences sPref = this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
		
		// CHECK UPDATE
		Handler mHandler = new Handler(Looper.getMainLooper());
		UpdateHelper uh = new UpdateHelper(this, getString(R.string.app_name), mHandler);
		uh.startThread();
		
		// Displays once a Disclaimer if accepted, else closes Application
		if(sPref.getBoolean(agb, true)){
			AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(this);
			aDialogBuilder.setTitle(getString(R.string.agb_title));
			//Accept
			aDialogBuilder.setMessage(getString(R.string.agb_text)).setPositiveButton(getString(R.string.alert_accept), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences sPref = getApplication().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sPref.edit();
					editor.putBoolean(agb, false);
					editor.commit();
					// Open the CoachMark
					onCoachMark(MainActivity.this);
				}
			//Decline
			}).setNegativeButton(getString(R.string.alert_decline), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences sPref = getApplication().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sPref.edit();
					editor.putBoolean(agb, true);
					editor.commit();
					android.os.Process.killProcess(android.os.Process.myPid());
					System.exit(1);
				}
			});
			AlertDialog aDialog = aDialogBuilder.create();
			aDialog.show();
		}
		
		LAYOUT_SIZE = findViewById(R.id.LinearLayoutGraph).getWidth();
		// Disables the keyboard at the start of the activity
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Spinner for Signal choice
		sig1_spin = (Spinner) findViewById(R.id.signal1Spinner);
		sig2_spin = (Spinner) findViewById(R.id.signal2Spinner);
		op_spin = (Spinner) findViewById(R.id.superposOperationSpinner);

		freq_bar = (SeekBar) findViewById(R.id.frequency);
		phase_bar = (SeekBar) findViewById(R.id.phase);
		freq_bar1 = (SeekBar) findViewById(R.id.frequency1);

		// Find the EditTexts
		freq_txt = (EditText) findViewById(R.id.textFreq);
		phase_txt = (EditText) findViewById(R.id.textPhase);
		freq_txt1 = (EditText) findViewById(R.id.textFreq1);

		// No focus at the start of the activity
		freq_txt.clearFocus();
		phase_txt.clearFocus();
		freq_txt1.clearFocus();

		// Spinner Adapter
		ArrayAdapter<CharSequence> sig1_spinner_adapter = ArrayAdapter.createFromResource(this, R.array.signal1_array, android.R.layout.simple_spinner_item);
		ArrayAdapter<CharSequence> sig2_spinner_adapter = ArrayAdapter.createFromResource(this, R.array.signal2_array, R.layout.spinner2_item);
		ArrayAdapter<CharSequence> op_spinner_adapter = ArrayAdapter.createFromResource(this, R.array.operation_array, android.R.layout.simple_spinner_item);

		sig1_spin.setAdapter(sig1_spinner_adapter);
		sig2_spin.setAdapter(sig2_spinner_adapter);
		op_spin.setAdapter(op_spinner_adapter);

		// Listeners for Spinners (Dropdown Menus)
		OnItemSelectedListener sig1_spin_listener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				signal1 = new ZeroSignal(); // default: zero
				if (position == 1){
					signal1 = new SinusSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.VISIBLE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
				}else if (position == 2){
					signal1 = new RectangularSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else if (position == 3){
					signal1 = new SawtoothSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else if (position == 4){
					signal1 = new TriangularSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else if (position == 5){
					signal1 = new SiSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else if (position == 6){
					signal1 = new OneSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else if (position == 7){
					signal1 = new ZeroSignal();
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
					selectedFreq1 = 2*Math.PI;
				}else{
					findViewById(R.id.freqContainer1).setVisibility(View.GONE);
					findViewById(R.id.equalButton).setVisibility(View.GONE);
					selectedFreq1 = 0;
				}
				arithmeticSignal.setSignalA(signal1);
				recalculateGraphs();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}

		};

		OnItemSelectedListener sig2_spin_listener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				signal2 = new ZeroSignal(); // default: zero
				if (position == 1)
					signal2 = new SinusSignal();
				else if (position == 2)
					signal2 = new RectangularSignal();
				else if (position == 3)
					signal2 = new SawtoothSignal();
				else if (position == 4)
					signal2 = new TriangularSignal();
				else if (position == 5)
					signal2 = new SiSignal();
				else if (position == 6)
					signal2 = new OneSignal();
				else if (position == 7)
					signal2 = new ZeroSignal();
				arithmeticSignal.setSignalB(signal2);
				recalculateGraphs();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}

		};

		OnItemSelectedListener op_spin_listener = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				if (position == 1) {
					arithmeticSignal = new DifferenceSignal(signal1, signal2);
					sumGraphView.setTitle(getString(R.string.graph_sum_lable_1) + getString(R.string.minus) + getString(R.string.graph_sum_lable_2));
				} else if (position == 2) {
					arithmeticSignal = new MultiplySignal(signal1, signal2);
					sumGraphView.setTitle(getString(R.string.graph_sum_lable_1) + getString(R.string.multiply) + getString(R.string.graph_sum_lable_2));
				} else if (position == 3) {
					arithmeticSignal = new DivisionSignal(signal1, signal2);
					sumGraphView.setTitle(getString(R.string.graph_sum_lable_1) + getString(R.string.divison) + getString(R.string.graph_sum_lable_2));
				} else {
					arithmeticSignal = new SumSignal(signal1, signal2);
					sumGraphView.setTitle(getString(R.string.graph_sum_lable_1) + getString(R.string.plus) + getString(R.string.graph_sum_lable_2));
				}
				recalculateGraphs();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		};

		sig1_spin.setOnItemSelectedListener(sig1_spin_listener);
		sig2_spin.setOnItemSelectedListener(sig2_spin_listener);
		op_spin.setOnItemSelectedListener(op_spin_listener);

		// Listeners for change in TextEdit
		phase_txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (actionByCode)
					return;
				int pos = 0;
				try {
					selectedPhase = Double.parseDouble(s.toString()) * Math.PI;
					// Reduce value to max/min
					if (selectedPhase > MAX_PHASE)
						selectedPhase = MAX_PHASE;
					if (selectedPhase < MIN_PHASE)
						selectedPhase = MIN_PHASE;
					// Calculate position of SeekBar adjuster
					pos = (int) ((selectedPhase - MIN_PHASE) * phase_bar.getMax() / (MAX_PHASE - MIN_PHASE));
					typing = true;
				} catch (Exception e) {
				}
				phase_bar.setProgress(pos);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// Listeners for change in TextEdit
		freq_txt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (actionByCode)
					return;
				int pos = 0;
				try {
					selectedFreq = Double.parseDouble(s.toString()) * Math.PI;
					// Reduce value to max/min
					if (selectedFreq > MAX_FREQ)
						selectedFreq = MAX_FREQ;
					if (selectedFreq < MIN_FREQ)
						selectedFreq = MIN_FREQ;
					// Calculate position of SeekBar adjuster
					pos = (int) ((selectedFreq - MIN_FREQ) * freq_bar.getMax() / (MAX_FREQ - MIN_FREQ));
					typing = true;
				} catch (Exception e) {
				}
				freq_bar.setProgress(pos);
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		//TODO: freq_txt listener
		freq_txt1.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (actionByCode)
					return;
				int pos = 0;
				try {
					selectedFreq1 = Double.parseDouble(s.toString()) * Math.PI;
					// Reduce value to max/min
					if (selectedFreq1 > MAX_FREQ)
						selectedFreq1 = MAX_FREQ;
					if (selectedFreq1 < MIN_FREQ)
						selectedFreq1 = MIN_FREQ;
					// Calculate position of SeekBar adjuster
					pos = (int) ((selectedFreq1 - MIN_FREQ) * freq_bar1.getMax() / (MAX_FREQ - MIN_FREQ));
					typing = true;
				} catch (Exception e) {
				}
				freq_bar1.setProgress(pos);
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		
		// Listener for focus
		phase_txt.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// Replaces input-value with rounded or max/min value
				if (!hasFocus) {
					actionByCode = true;
					phase_txt.setText("" + round(selectedPhase / Math.PI));
					actionByCode = false;
				}
			}
		});

		// Listener for focus
		freq_txt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// Replaces input-value with rounded or max/min value
				if (!hasFocus) {
					actionByCode = true;
					freq_txt.setText("" + round(selectedFreq / Math.PI));
					actionByCode = false;
				}
			}
		});
		
		//TODO: freq_txt Listener
		freq_txt1.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// Replaces input-value with rounded or max/min value
				if (!hasFocus) {
					actionByCode = true;
					freq_txt1.setText("" + round(selectedFreq1 / Math.PI));
					actionByCode = false;
				}
			}
		});
		
		// Listener for change on the Frequency-SeekBar
		OnSeekBarChangeListener listener_freq = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				selectedFreq = (MAX_FREQ - MIN_FREQ) * progress / seekBar.getMax() + MIN_FREQ;
				TextView t = (TextView) findViewById(R.id.soundHz_edit);
				t.setText("" + round((selectedFreq+selectedFreq1) / Math.PI * 100));
				recalculateGraphs();
			}
		};

		// Listener for change on the Phase-SeekBar
		OnSeekBarChangeListener listener_phase = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				selectedPhase = (MAX_PHASE - MIN_PHASE) * progress / seekBar.getMax() + MIN_PHASE;
				recalculateGraphs();
			}
		};
		
		//TODO: freq_bar1 listener
		OnSeekBarChangeListener listener_freq1 = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				selectedFreq1 = (MAX_FREQ - MIN_FREQ) * progress / seekBar.getMax() + MIN_FREQ;
				TextView t = (TextView) findViewById(R.id.soundHz_edit);
				t.setText("" + round((selectedFreq1+selectedFreq1) / Math.PI * 100));
				recalculateGraphs();
			}
		};
		
		// Set seekbars on their listeners
		freq_bar.setOnSeekBarChangeListener(listener_freq);
		phase_bar.setOnSeekBarChangeListener(listener_phase);
		freq_bar1.setOnSeekBarChangeListener(listener_freq1);
		
		//TODO: equalButton
		//Set freq:sig2 to freq:sig1
		equalButton = (Button) findViewById(R.id.equalButton);
		equalButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str = freq_txt1.getText().toString();
				freq_txt.setText(freq_txt1.getText());
				//freq_bar.setProgress((int) ((Double.parseDouble(str) - MIN_FREQ) * freq_bar1.getMax() / (MAX_FREQ - MIN_FREQ)));
			}
		});
		

		// Two Grids for displaying different sin curves
		// Displays one static sin curve and one with alternating phase and
		// frequency modified by user
		graphView = new LineGraphView(getApplicationContext(), getString(R.string.graph_lable));
		sumGraphView = new LineGraphView(getApplicationContext(), getString(R.string.graph_sum_lable_1) + getString(R.string.plus) + getString(R.string.graph_sum_lable_2));

		// GraphViewData is container for raw graph data
		GraphViewData[] dataStatic = new GraphViewData[Signal.GRAPH_SAMPLES];
		for (int i = 0; i < Signal.GRAPH_SAMPLES; i++) {
			dataStatic[i] = new GraphViewData(i, 0);
		}

		signal1 = new ZeroSignal();
		signal2 = new ZeroSignal();
		arithmeticSignal = new SumSignal(signal1, signal2);
		// Curve data
		seriesSinStatic = new GraphViewSeries("1", new GraphViewSeriesStyle(Color.BLACK, 3), dataStatic);
		seriesSinMod = new GraphViewSeries("2", new GraphViewSeriesStyle(Color.BLUE, 3), dataStatic);
		seriesSum = new GraphViewSeries("3", new GraphViewSeriesStyle(Color.RED, 3), dataStatic);

		// Add the graph series to the grid
		graphView.addSeries(seriesSinStatic);
		graphView.addSeries(seriesSinMod);
		sumGraphView.addSeries(seriesSum);

		// Set the y-axis to maximum values
		graphView.setManualYAxisBounds(1, -1);
		sumGraphView.setManualYAxisBounds(2, -2);

		// Set grid legend enabled
		graphView.setShowLegend(true);
		// sumGraphView.setShowLegend(true);

		// Set custom horizontal/vertical labels for the Grid
		graphView.setHorizontalLabels(new String[] { "0", "", "1" + "\u03C0", "", "2" + "\u03C0", "", "3" + "\u03C0", "", "4" + "\u03C0", "", "5" + "\u03C0", "", "6" + "\u03C0" });
		graphView.setVerticalLabels(new String[] { "A", "0", "-A" });

		sumGraphView.setHorizontalLabels(new String[] { "0", "", "1" + "\u03C0", "", "2" + "\u03C0", "", "3" + "\u03C0", "", "4" + "\u03C0", "", "5" + "\u03C0", "", "6" + "\u03C0" });
		sumGraphView.setVerticalLabels(new String[] { "2A", "A", "0", "-A", "-2A" });

		// Set the colors of the Grid (vLable, hLable, grid)
		graphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));
		sumGraphView.setGraphViewStyle(new GraphViewStyle(Color.BLACK, Color.BLACK, Color.BLACK));

		// Add the grids to the activity
		LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayoutGraph);
		layout.addView(graphView);
		LinearLayout layoutSum = (LinearLayout) findViewById(R.id.LinearLayoutSumGraph);
		layoutSum.addView(sumGraphView);

		// Add Gestures for controlling the Graphs
		gestrureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

				if ((e1.getX() - e2.getX()) / LAYOUT_SIZE > 0.1)
					selectedPhase += 0.5;
				else if ((e1.getX() - e2.getX()) / LAYOUT_SIZE < -0.1)
					selectedPhase -= 0.5;

				if (selectedPhase > 2 * Math.PI)
					selectedPhase = 0;
				if (selectedPhase < 0)
					selectedPhase = Math.PI * 2;

				recalculateGraphs();
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
		});

		layout.setOnTouchListener(new View.OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				gestrureDetector.onTouchEvent(event);
				return true;
			}
		});

		t = new Thread() {
			@Override
			public void run() {
				// create an audiotrack object
				AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Signal.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, Signal.BUFFER_SIZE, AudioTrack.MODE_STREAM);

				byte samples[] = new byte[Signal.BUFFER_SIZE];
				byte samples_zero[] = new byte[Signal.BUFFER_SIZE];
				// start audio
				audioTrack.write(samples_zero, 0, Signal.BUFFER_SIZE);
				audioTrack.play();

				// synthesis loop
				while (isRunning) {
					if (toggleSound) {
						samples = arithmeticSignal.getAudioBytes();
						audioTrack.write(samples, 0, Signal.BUFFER_SIZE);
						// } else {
						// audioTrack.write(samples_zero, 0,
						// Signal.BUFFER_SIZE);
					}
				}
				audioTrack.stop();
				audioTrack.release();
			}
		};
		t.start();
		recalculateGraphs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_signal) {
			if (toggleSignal) {
				item.setIcon(R.drawable.ic_action_signal_white_on);
				findViewById(R.id.signalContainer).setVisibility(View.VISIBLE);
				toggleSignal = !toggleSignal;
			} else {
				item.setIcon(R.drawable.ic_action_signal_white_off);
				findViewById(R.id.signalContainer).setVisibility(View.GONE);
				toggleSignal = !toggleSignal;
			}
			return true;
		} else if (item.getItemId() == R.id.action_freqnecy) {
			if (toggleFrequency) {
				item.setIcon(R.drawable.ic_action_frequnecy_white_on);
				findViewById(R.id.freq_buttonContainer).setVisibility(View.VISIBLE);
				findViewById(R.id.equalButton).setVisibility(View.VISIBLE);
				toggleFrequency = !toggleFrequency;
			} else {
				item.setIcon(R.drawable.ic_action_frequnecy_white_off);
				findViewById(R.id.freq_buttonContainer).setVisibility(View.GONE);
				findViewById(R.id.equalButton).setVisibility(View.GONE);
				toggleFrequency = !toggleFrequency;
			}
			return true;
		} else if (item.getItemId() == R.id.action_phase) {
			if (togglePhase) {
				item.setIcon(R.drawable.ic_action_phase_white_on);
				findViewById(R.id.phaseContainer).setVisibility(View.VISIBLE);
				togglePhase = !togglePhase;
			} else {
				item.setIcon(R.drawable.ic_action_phase_white_off);
				findViewById(R.id.phaseContainer).setVisibility(View.GONE);
				togglePhase = !togglePhase;
			}
			return true;
		} else if (item.getItemId() == R.id.action_sound) {
			SharedPreferences sPref = this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
			final MenuItem itemTransfer = item;
			// ALERTDIALOG: Executed only on first Sound run.
			if (sPref.getBoolean(onFirstSound, true)) {
				AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(this);
				aDialogBuilder.setTitle(getString(R.string.alert_title));
				aDialogBuilder.setMessage(getString(R.string.alert_text)).setPositiveButton(getString(R.string.alert_accept), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sPref = getApplication().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sPref.edit();
						editor.putBoolean(onFirstSound, false);
						editor.commit();
						itemTransfer.setIcon(R.drawable.ic_action_sound_white_on);
						findViewById(R.id.SoundContainer).setVisibility(View.VISIBLE);
						toggleSound = !toggleSound;
					}
				}).setNegativeButton(getString(R.string.alert_decline), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences sPref = getApplication().getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sPref.edit();
						editor.putBoolean(onFirstSound, true);
						editor.commit();
						dialog.cancel();
					}
				});
				AlertDialog aDialog = aDialogBuilder.create();
				aDialog.show();
			}
			Log.d("Sound", "onFirstSound: " + !sPref.getBoolean(onFirstSound, true) + " ToggleSound: " + toggleSound);
			if (!sPref.getBoolean(onFirstSound, true)) {
				item.setIcon(toggleSound ? R.drawable.ic_action_sound_white_off : R.drawable.ic_action_sound_white_on);
				findViewById(R.id.SoundContainer).setVisibility(toggleSound ? View.GONE : View.VISIBLE);
				toggleSound = !toggleSound;
			}
			return true;
		} else if (item.getItemId() == R.id.action_help) {
			// Start CoachMark again
			onCoachMark(this);
			return true;
		} else if (item.getItemId() == R.id.action_about_us) {
			// Open new Activity for AboutUs
			Intent intent = new Intent(this, AboutUsActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onPause() {
		super.onPause();
		toggleSound = false;
	}

	public void onResume() {
		toggleSound = false;
		findViewById(R.id.SoundContainer).setVisibility(View.GONE);
		super.onResume();
	}

	public void onStop() {
		super.onStop();
		toggleSound = false;
	}

	public void onStart() {
		super.onStart();
		toggleSound = false;
	}
}
