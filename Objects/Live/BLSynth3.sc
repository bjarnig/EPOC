
BLSynth3 : BGen
{ 	
	var paramValues, reverb, effectBus, <>isPlaying, preControl;	
	*new { |id=0, description, duration=10, control, outBus=0, values, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values).init(load);
	}
	
	init {|load=1|
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new; 
		if(this.control.isNil, {this.control = BControl.new});
		if(load > 0, { this.initEffect.value });
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \outBus, {outBus = paramValue});
	if(paramName == \duration, {duration = paramValue});
	}
	
	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLSynth3Verb, [\in, effectBus, \out, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\bLSynth3,
		{| out=0, atk=0.5, sus=1, rel=0.1, surface=0.0, harmAmp=0.0, speed=0.0, entropy=0.5, pan=0.0, bpFreq=1000,
		freq1=80, freq2=90, freq3=100, freq4=110, freq5=120, freq6=130, freq7=140, freq8=150, amp=0.5, amp1=0.5, amp2=0.5, amp3=0.5, amp4=0.5, amp5=0.5, amp6=0.5, amp7=0.5, amp8=0.5|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var osca = Impulse.ar(freq1 + LFNoise1.ar(100, entropy * 100, amp1)) * amp1;
		var oscb = Impulse.ar(freq2 + LFNoise1.ar(200, entropy * 200, amp2)) * amp2;
		var oscc = Impulse.ar(freq3 + LFNoise1.ar(300, entropy * 300, amp3)) * amp3;
		var oscd = Impulse.ar(freq4 + LFNoise1.ar(400, entropy * 400, amp4)) * amp4;
		var osce = Impulse.ar(freq1 + LFNoise1.ar(500, entropy * 500, amp5)) * amp5;
		var oscf = Impulse.ar(freq2 + LFNoise1.ar(600, entropy * 600, amp6)) * amp6;
		var oscg = Impulse.ar(freq3 + LFNoise1.ar(700, entropy * 700, amp7)) * amp7;
		var osch = Impulse.ar(freq4 + LFNoise1.ar(800, entropy * 800, amp8)) * amp8;
		var signal = (osca + oscb + oscc + oscd + osce + oscf + oscg + osch);
				
		signal = (signal * surface) + ((1 - surface) * Formlet.ar(signal, freq1, 0.01, 0.1));
		signal = (signal * (1-speed)) + (signal * StandardL.ar(50*entropy, 10*speed) * speed);
		signal = BPeakEQ.ar(signal, bpFreq, 2, 6);
		signal = LeakDC.ar(signal);
		signal = signal * env;
		signal = signal * amp;
		signal = Pan2.ar(signal, pan, 1);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add; 
		
		SynthDef(\bLSynth3Verb,{| Êd1 = 0.1, d2 = 0.15, d3 = 0.2, d4 = 0.25, d5 = 0.4,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 450, f2 = 850, f3 = 1250, f4 = 2450, 
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0, mix=0.5|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(in, 2);

	  	c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult) * 1.2;
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult) * 1.2;
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult) * 1.2;
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult) * 1.2;
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult) * 1.2;
Ê Ê Ê Ê
Ê Ê Ê Ê	outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * mix) + (inB * (1 - mix));

Ê Ê Ê Ê	Out.ar(out, outB * amp);

	}).add;
	
	}
	
	play {
	
	this.stop.value; 
	
	if(this.isPlaying == 0,  
	{
		this.playWrap.value;
		this.update(1).value;
		this.isPlaying = 1;
	}, 
	{ this.update(0).value; });
	}
	
	playWrap {
	var atk, sus, rel;	
	
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap = Bwrap.new(\bLSynth3, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('BLSynth3 start'.postln);
		wrap.play;
	}
	
	stop {arg release=0;
	var ampstep, delta, amp, steps;

		if(release > 0 && this.isPlaying == 1, {
		
		delta = 0.1;
		amp = wrap.get(\amp, amp);
		steps = release / delta;
		ampstep = amp / steps;
		
		Routine {
		steps.do {
		amp = amp - ampstep;
		if(wrap.synth.isPlaying, {wrap.set(\amp, amp);});
		delta.wait;
		};
		
		wrap.stop;		
		this.isPlaying = 0;
		}.play;
		
		}, {
		
		if(this.isPlaying == 1, {
		if(wrap.synth.isPlaying, {wrap.stop});
		this.isPlaying = 0;});
		
		});
	}
	
	dispose {
		
		if(this.isPlaying == 1, {this.stop.value});
		this.freeEffect;
		this.isPlaying = 0;
	}
	
	update {arg updateAll=0;
	var newFrequency1, newFrequency2, newFrequency3, newFrequency4, newFrequency5, newFrequency6, newFrequency7, newFrequency8;
	var newAttack, newRelease, newSustain, newAmp, newharmAmp, newSpeed, ampWrapVals, ampWrapTimes;
	
	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.entropy != preControl.entropy) || (control.density != preControl.density), 
	{
		newFrequency1 = Env.new([0.0, 10, 20, 50, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		newFrequency2 = newFrequency1 + (control.density * 20 * 1);
		newFrequency3 = newFrequency1 + (control.density * 20 * 2);
		newFrequency4 = newFrequency1 + (control.density * 20 * 3);
		newFrequency5 = newFrequency1 + (control.density * 20 * 4);
		newFrequency6 = newFrequency1 + (control.density * 20 * 5);
		newFrequency7 = newFrequency1 + (control.density * 20 * 6);
		newFrequency8 = newFrequency1 + (control.density * 20 * 7);
	
		wrap.set(\freq1, newFrequency1);
		wrap.set(\freq2, newFrequency2);
		wrap.set(\freq3, newFrequency3);
		wrap.set(\freq4, newFrequency4);
		wrap.set(\freq5, newFrequency5);
		wrap.set(\freq6, newFrequency6);
		wrap.set(\freq7, newFrequency7);
		wrap.set(\freq8, newFrequency8);
	});
	
	if(updateAll == 1 || (control.entropy != preControl.entropy), 
	{	
		wrap.set(\entropy, Env.new([0.0, 0.2, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});
	
	if(updateAll == 1 || (control.attack != preControl.attack), 
	{	
		newAttack = control.attack * duration;
		wrap.set(\atk, newAttack);
	});

	if(updateAll == 1 || (control.release != preControl.release), 
	{	
		newRelease = control.release * duration * 0.9;
		wrap.set(\rel, newRelease);
	});

	if(updateAll == 1 || (control.attack != preControl.attack) || (control.release != preControl.release), 
	{
		'update sustain'.postln;
		newSustain = duration - (newAttack + newRelease);
		wrap.set(\sus, newSustain);
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		newAmp = Env.new([0.0, 0.2, 0.5, 1.25, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		wrap.set(\amp, newAmp);
	});
	
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{
		newSpeed = control.speed.linexp(0.0, 1.0, 0.001, 25.0);
		wrap.set(\speed, newSpeed);
	});
		
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\mix, control.color);
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		wrap.set(\surface, control.surface);
	});
	
	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\pan, control.location.linlin(0.0, 1.0, -1.0, 1.0));
	});
	
	if(updateAll == 1 || (control.position != preControl.position), 
	{
		ampWrapVals = [1.0, 0.875, 0.75, 0.625, 0.5, 0.375, 0.25, 0.125, 0.001];
		ampWrapTimes = [0.143, 0.143, 0.143, 0.143, 0.143, 0.143, 0.143];
		wrap.set(\bpFreq, Env.new([60, 200, 800, 4000, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.position));
		wrap.set(\amp1, Env.new(ampWrapVals, ampWrapTimes).at(control.position));
		wrap.set(\amp2, Env.new(ampWrapVals.rotate(1), ampWrapTimes).at(control.position));
		wrap.set(\amp3, Env.new(ampWrapVals.rotate(2), ampWrapTimes).at(control.position));
		wrap.set(\amp4, Env.new(ampWrapVals.rotate(3), ampWrapTimes).at(control.position));
		wrap.set(\amp5, Env.new(ampWrapVals.rotate(4), ampWrapTimes).at(control.position));
		wrap.set(\amp6, Env.new(ampWrapVals.rotate(5), ampWrapTimes).at(control.position));
		wrap.set(\amp7, Env.new(ampWrapVals.rotate(6), ampWrapTimes).at(control.position));
		wrap.set(\amp8, Env.new(ampWrapVals.rotate(7), ampWrapTimes).at(control.position));
	});
	
	preControl.copy(this.control);
	
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BLSynth3";
	}
}