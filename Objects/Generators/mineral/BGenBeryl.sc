
BGenBeryl : BGen
{ 	
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>durations, <>amplitudes, <>osc, <>curveType;	
	
	*new { |id=0, description, duration=10, control, outBus=0, values, durations, amplitudes, osc=0, curveType=0, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, durations, amplitudes, osc, curveType).init(load);
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new; 
		if(this.control.isNil, {this.control = BControl.new});
		
		if(load > 0, {
		
		this.initEffect.value;
		
		if(durations.isNil, {
			durations = [1.5, 1, 2, 4, 0.5, 1];
			if(this.curveType == 1, {durations = [4, 2, 1, 0.5, 0.25, 0.15]}); 
			if(this.curveType == 2, {durations = [1.5, 0.5, 2, 4, 2, 1.5]}); 
			if(this.curveType == 3, {durations = [0.25, 0.75, 0.25, 1, 2, 4]}); 
			if(this.curveType == 4, {durations = [1.5, 1, 2, 4, 1.5, 1]}); 
		});
		
		if(amplitudes.isNil, {
			amplitudes = [0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1];
			if(this.curveType == 1, {amplitudes = [0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1]});
			if(this.curveType == 2, {amplitudes = [0.1, 1.0, 0.13, 1.0, 0.4, 0.9, 0.1]});
			if(this.curveType == 3, {amplitudes = [0.05, 1.0, 0.05, 0.8, 0.01, 1.0, 0.05]});
			if(this.curveType == 4, {amplitudes = [1.0, 0.4, 0.99, 0.0, 1.0, 0.2, 1.0]});
			});
		});
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \amplitudes, {amplitudes = paramValue});
	if(paramName == \osc, {osc = paramValue});
	if(paramName == \curveType, {curveType = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\berylVerb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
				
		SynthDef(\beryl, {arg  outBus=0, amp=0.5, atk=0.5, sus=10, rel=0.1, freq=1000, pan=0.8, entropy=0.5, entropySpeed=0.5,
		envTime=8, envShape=2, osc=0, dryWet, lop=18000, hip=20, lopVol=0.5, hipVol=0.5,
		durations=#[1.5, 1, 2, 4, 0.5, 1], amplitudes=#[0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1];
		var signal, dry, env, amp1=0.8 * amp, amp2=4.2 * amp;
		signal = Select.ar(osc, [(LinCongC.ar(freq) + LinCongC.ar(freq * 0.5) + LinCongC.ar(freq * 2.0)) * amp1,
		(HenonC.ar(freq) + HenonC.ar(freq * 0.5) + HenonC.ar(freq * 2.0) * 0.1) * amp2]);  
		signal = BLowPass4.ar(signal, lop, 0.25, mul:lopVol) + BHiPass4.ar(signal, hip, 0.25, mul:hipVol);
		env = EnvGen.kr(Env.new(amplitudes, durations, [envShape, envShape * (-1)]).circle, timeScale:envTime) * LFNoise1.kr(200 * entropySpeed, entropy, 1.0);
		signal = signal * env; 
		signal = Pan2.ar(signal, pan);
		Out.ar(outBus, signal);
		}).add;

		SynthDef(\berylVerb, { 
		arg dryWet=0.2, inBus=0, outBus=2;
		var output, signal, verb, effect;
		signal = In.ar(inBus, 2);	
		verb = signal;
		8.do{verb=AllpassL.ar(verb,0.3,{0.2.rand+0.1}!2,5)};
 		verb = verb.tanh;
		effect = BLowPass4.ar(verb * 0.5, 1400, 0.2) + BHiPass4.ar(verb * 0.5, 8000, 0.2);
		output = (effect * dryWet) + (signal * (1-dryWet)); 
		Out.ar(outBus, output);
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
		wrap = Bwrap.new(\beryl, paramValues);
		wrap.setValues([\outBus, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude, \osc, this.osc]);
		('Beryl start'.postln);
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
		if(wrap.synth.isPlaying, {wrap.stop;});
		this.isPlaying = 0;});
		
		});
	}
	
	dispose {
		
		if(this.isPlaying == 1, {this.stop.value});
		this.freeEffect;
		this.isPlaying = 0;
	}
	
	update {arg updateAll=0;
	var newAttack, newSustain, newRelease;
		
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{
		wrap.set(\envTime, Env.new([10, 5, 1, 0.5, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\entropySpeed, Env.new([0.01, 0.25, 0.5, 0.55, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.speed)); 
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{	
		wrap.set(\envShape, Env.new([8, 4, 1, 0.5, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});
	
	if(updateAll == 1 || (control.entropy != preControl.entropy), 
	{	
		wrap.set(\entropy, Env.new([0.0, 0.25, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});
	
	if(updateAll == 1 || (control.position != preControl.position), 
	{	 
		wrap.set(\durations, BUtils.limitArrayByPositionSameSize(control.position, durations));
		wrap.set(\amplitudes, BUtils.limitArrayByPositionSameSize(control.position, amplitudes));
	});
	
	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.density != preControl.density), 
	{
		wrap.set(\freq, Env.new([100, 8000, 20000],[0.5, 0.5]).at(control.frequency));
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		wrap.set(\amp, Env.new([0.0, 0.05, 0.2, 0.4, 0.55],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});
	
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\dryWet, control.color);
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		wrap.set(\lop, Env.new([100, 250, 2000, 400, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hip, Env.new([8000, 2000, 4000, 6000, 10000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\lopVol, Env.new([0.7, 0.6, 0.5, 0.25, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hipVol, Env.new([0.2, 0.4, 0.5, 0.75, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
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
		description = "BGenBeryl";
	}
}