BControlTest
{
	addSynthDef{
	
	SynthDef(\testTone, {|freq = 1000| 
	Out.ar(0, Pan2.ar(SinOsc.ar(freq, 0, 0.2), 0.5, 0.5) * EnvGen.kr(Env.perc, 1.2, doneAction: 2) ) }).add;
	
	}
	
	initSpeach{
	
	Speech.init(2);
	Speech.channels[0].volume_(-15.dbamp);
	Speech.channels[1].volume_(-15.dbamp);
		
	}
	
	testSpeedDensity { arg item, duration=5;
	
	this.addSynthDef.value;
	this.initSpeach.value;
		
	Routine
	{
	1.do({arg a;	
	
	item.control.speed = 0.9;
	item.control.density = 0.1;
	item.duration = duration;
	
	("TESTING NOW: Hi Speed, Lo Density").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.speed = 0.1;
	item.control.density = 0.9;
	item.duration = duration;
	("TESTING NOW: Lo Speed, Hi Density").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.speed = 0.9;
	item.control.density = 0.9;
	item.duration = duration;
	("TESTING NOW: Hi Speed, Hi Density").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.speed = 0.1;
	item.control.density = 0.1;
	item.duration = duration;
	("TESTING NOW: Lo Speed, Lo Density").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	})	
	}.play;
	}
	
	testFrequencySurface { arg item, duration=5;
		
	this.addSynthDef.value;
	this.initSpeach.value;
	
	Routine
	{
	1.do({arg a;	
	
	item.control.frequency = 0.9;
	item.control.surface = 0.1;
	item.duration = duration;
	("TESTING NOW: Hi frequency, Lo surface").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.frequency = 0.1;
	item.control.surface = 0.9;
	item.duration = duration;
	("TESTING NOW: Lo frequency, Hi surface").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.frequency = 0.9;
	item.control.surface = 0.9;
	item.duration = duration;
	("TESTING NOW: Hi frequency, Hi surface").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.frequency = 0.1;
	item.control.surface = 0.1;
	item.duration = duration;
	("TESTING NOW: Lo frequency, Lo surface").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
		
	})	
	}.play;
	}
	
	testLocationColor { arg item, duration=5;
	
	this.addSynthDef.value;
	this.initSpeach.value;
	
	Routine
	{
	1.do({arg a;	
	
	item.control.location = 0.9;
	item.control.color = 0.1;
	item.duration = duration;
	("TESTING NOW: Hi location, Lo color").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.location = 0.1;
	item.control.color = 0.9;
	item.duration = duration;
	("TESTING NOW: Lo location, Hi color").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.location = 0.9;
	item.control.color = 0.9;
	item.duration = duration;
	("TESTING NOW: Hi location, Hi color").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.location = 0.1;
	item.control.color = 0.1;
	item.duration = duration;
	("TESTING NOW: Lo location, Lo color").speak;
	(5).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	})	
	}.play;
	}
	
	testEntropyAttackRelease { arg item, duration=5;
	
	this.addSynthDef.value;
	this.initSpeach.value;
	
	Routine
	{
	1.do({arg a;	
	
	item.control.entropy = 0.9;
	item.control.attack = 0.001;
	item.control.release = 0.8;
	item.duration = duration;
	("TESTING NOW: Hi entropy, Lo attack, Hi release").speak;
	(6).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.entropy = 0.1;
	item.control.attack = 0.7;
	item.control.release = 0.001;
	item.duration = duration;
	("TESTING NOW: LO entropy, Hi attack, Lo release").speak;
	(6).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.entropy = 0.9;
	item.control.attack = 0.001;
	item.control.release = 0.001;
	item.duration = duration;
	("TESTING NOW: Hi entropy, Lo attack, Lo release").speak;
	(6).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	item.control.entropy = 0.01;
	item.control.attack = 0.5;
	item.control.release = 0.5;
	item.duration = duration;
	("TESTING NOW: Lo entropy, Hi attack, Hi release").speak;
	(6).wait;
	item.play;
	(duration + 1).wait;
	Synth(\testTone);
	(2).wait;
	
	})	
	}.play;
	}
}