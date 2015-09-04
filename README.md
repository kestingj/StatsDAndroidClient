Android Library for Collecting Metrics with StatsD

USAGE
=====

Step 1: Configure Android Manifest

Add the following to AndroidManifest.xml

<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

AND

<receiver android:name=".NetworkChangeReceiver">
    <intent-filter>
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
        <action android:name="android.net.wifi.WIFI_STATE_CHANGED" />
    </intent-filter>
</receiver>

inside the <application></application> tags.


Step 2: Initialize StatsD Client within onCreate()

NonBlockingStatsDClient statsD;

@Override
protected void onCreate (Bundle savedInstanceState) {
	
	StatsDClientKey key = new StatsDClientKey ("example.prefix", "www.exampledomain.com", portNumber);
	statsD = StatsDClientContainer.INSTANCE.getClient(key);
	...
	...

}

Step 3: Override onResume() 

@Override
public void onResume() {
	super.onResume();
	NetworkStateManager.testNetworkConnectionChange(getApplicationContext());

	...
	...
}

Step 4: Send Metrics

-Send counter:

statsD.count("metric.name", metricValue);

-Send timer:
statsD.recordExecutionTime("metric.name", metricValue);

-Send set 

statsD.recordSetEvent("metric.name", "eventName");

-Send gauge

statsD.recordGaugeValue("metric.name", metricValue);

Step 5: Close StatsD Client within onDestroy()

@Override
protected void onDestroy() {
	statsDClientContainer.closeClient(client);

	OR

	statsDClientContainer.closeClient("prefix", "domain", port);

	...
	...
}

Scope
=====
All NonBlockingStatsDClient objects are shared across the project. If two 
calls to create client are made from different activites with the same 
prefix, domain, and port a reference to the same object will be returned in 
both cases. 

Wiki
====
https://w.amazon.com/index.php/Amazon_Music/Componentization/Europa

Maintainers
===========
@kestingj


