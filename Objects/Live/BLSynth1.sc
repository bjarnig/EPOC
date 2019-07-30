
BLSynth1 : BGen
{ 	
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>durations, <>amplitudes, <>curveType, <>shaperBuf;	
	
	*new { |id=0, description, duration=10, control, outBus=0, values, shaperBuf, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, shaperBuf).init(load);
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new; 
		if(this.control.isNil, {this.control = BControl.new});
		
		if(load > 0, {
		
		this.shaperBuf = Buffer.alloc(Server.local, 512, 1, { |buf| buf.chebyMsg([0.25,0.75,0.25, 0.5, 0.125, 0.5])});
		this.initEffect.value;
		
		});
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \shaperBuf, {shaperBuf = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLSynth1Verb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
				
	SynthDef(\bLSynth1, {arg outBus=0, atk=0.1, sus=400, rel=4, speed = 0.11, density = 0.8525, dryWet=0.94, pan=0.1, entropy=0.071, amp=0.74, pos=0.575, surface=0.1, 
	freqA=80, freqB=52, freqBFilter=200, freqC=1200, freqD=2000, ampA=0.5, ampB=05, ampC=0.5, ampD=0.5, shaperBuf; 
	var osca, oscb, oscc, oscd, signal, verb, output, distortion, env;
	env = EnvGen.kr(Env.new([0, 1, 1, 0],[atk, sus, rel])); 
	osca = SinOsc.ar(freqA, 0.3) * ampA;
	oscb = RLPF.ar(Impulse.ar(freqB, mul:0.5), freqBFilter, 0.1) * ampB;
	oscc = HPF.ar(Dust.ar(180.5), freqC) * ampC;
	oscd = HPF.ar(PinkNoise.ar(0.5), freqD) * ampD;
	signal = osca + oscb + oscc + oscd;
	signal = (signal * LFGauss.ar(speed, density,0,1)) * 1.5;
	signal = signal * LFNoise2.ar(100 * entropy, 0.5, 0.5).min(1.0).max(1 - entropy);
	// distortion = HPF.ar(Shaper.ar(shaperBuf, signal, 0.5), 60); 
	distortion = Shaper.ar(shaperBuf, signal, 0.5); 
	signal = (signal * (1 - surface)) + (distortion * surface);
	//signal = HPF.ar(signal, 80);
	signal = signal * env;
	output =  Pan2.ar(signal, pan);
	output = output * amp;
	Out.ar(outBus, output);
	}).store;

	SynthDef(\bLSynth1Verb, { 
	arg dryWet=0.2, inBus=0, outBus=2;
	var output, signal, verb, effect, del;
	signal = In.ar(inBus, 2);
	10.do {
		del = 0.2.rand;
		verb = AllpassN.ar(signal,del,del,5);
	};
	effect = (verb*0.5) + (GVerb.ar(signal, roomsize: 75, revtime: 8, damping: 0.9, spread: 0.25, drylevel: 0.5, earlyreflevel:0.3, taillevel:0.6) * 0.8);
	output = (signal * (1 - dryWet)) + (effect * dryWet);
	Out.ar(outBus, output);
	}).store;
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
		wrap = Bwrap.new(\bLSynth1, [\shaperBuf, this.shaperBuf]);
		wrap.setValues([\outBus, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude, \shaperBuf, this.shaperBuf]);
		('BLSynth1 start'.postln);
		wrap.play;
	}
	
	stop {arg release=0;
	var ampstep, delta, amp, steps;
	('SYNTH 1 release is ' ++ release).postln; 
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
		if(wrap.synth.isPlaying, {wrap.stop;});
		this.isPlaying = 0;});
		
		});
	}
	
	dispose {
		
		if(this.isPlaying == 1, {this.stop.value});
		this.shaperBuf.free;
		this.freeEffect;
		this.isPlaying = 0;
	}
	
	update {arg updateAll=0;
	var newAttack, newSustain, newRelease;
		
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{
		wrap.set(\speed, Env.new([10, 3, 0.001],[0.5, 0.5]).at(control.speed));
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{	
		wrap.set(\density, Env.new([0.008, 0.5, 0.9],[0.5, 0.5]).at(control.density));
	});
	
	if(updateAll == 1 || (control.entropy != preControl.entropy), 
	{	
		wrap.set(\entropy, Env.new([0.001, 0.5, 0.9],[0.5, 0.5]).at(control.entropy));
	});
	
	if(updateAll == 1 || (control.position != preControl.position), 
	{	 
		wrap.set(\ampA, Env.new([0.6, 0.0],[1.0]).at(control.position));
		wrap.set(\ampB, Env.new([0.6, 0.0],[1.0]).at(control.position));
		wrap.set(\ampC, Env.new([0.0, 1.2],[1.0]).at(control.position));
		wrap.set(\ampD, Env.new([0.0, 1.2],[1.0]).at(control.position));
	});
	
	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.density != preControl.density), 
	{
		wrap.set(\freqA, Env.new([70, 95],[1.0]).at(control.frequency));
		wrap.set(\freqB, Env.new([18, 80],[1.0]).at(control.frequency));
		wrap.set(\freqBFilter, Env.new([200, 2000],[1.0]).at(control.frequency));
		wrap.set(\freqC, Env.new([1000, 2000],[1.0]).at(control.frequency));
		wrap.set(\freqD, Env.new([2000, 4000],[1.0]).at(control.frequency));
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		wrap.set(\amp, Env.new([0.0, 0.25, 1.0, 3.0, 8.0],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});
	
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\dryWet, control.color);
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		wrap.set(\surface, Env.new([0.0, 0.25, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});
	
	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\pan, control.location.linlin(0.0, 1.0, -0.8, 0.8));
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
		newSustain = duration - (newAttack + newRelease);
		wrap.set(\sus, newSustain);
	});
	
	preControl.copy(this.control);
	
	}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BLSynth1";
	}
}