
BGenEthyl : BGen
{ 	
	var <>durations, paramValues, reverb, effectBus, <>isPlaying, preControl, irspectrum, <>impulse;	
	
	*new { |id=0, description, duration=10, control, outBus=0, values, durations, impulse, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, durations, impulse).init(load);
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new;
		if(this.control.isNil, {this.control = BControl.new});
		if(load > 0, { this.initEffect.value });
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	if(paramName == \impulse, {impulse = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	initEffect {
		var ir, irbuffer, bufsize, fftsize=1024;
		Routine {
		if(durations.isNil, {this.durations = [1, 0.9, 1.1, 0.8, 1.2, 0.7, 1.3, 0.6, 1.4, 4]});
		if(impulse.isNil, {this.impulse = BConstants.impulseResponse});
		effectBus = Bus.audio(Server.local, 2);
		irbuffer= Buffer.read(Server.local, impulse);
		Server.local.sync;
		bufsize= PartConv.calcBufSize(fftsize, irbuffer); 
		irspectrum= Buffer.alloc(Server.local, bufsize, 1);
		irspectrum.preparePartConv(irbuffer, fftsize);
		Server.local.sync;
		irbuffer.free; 
		reverb = Bwrap.new(\ethylVerb, [\in, effectBus, \out, outBus, \fftsize, fftsize, \specBufNum ,irspectrum.bufnum]);
		reverb.play;}.play;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\ethyl,
		{| out=0, freq1=80, freq2=90, freq3=100, freq4=110, amp=0.5, atk=0.5, sus=1, rel=0.1, density=0.0, surface=0.0, speed=0.0, entropy=0.5, pan=0, pos=0.0,
		speedSeq=#[1, 0.9, 1.1, 0.8, 1.2, 0.7, 1.3, 0.6, 1.4, 0.5]| 
	 	
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var signal, seq, durSeq, trig, newSpeed, addImpulse, addDust;
	 	
	 	speedSeq = speedSeq * speed;
	 	seq = Dseq(speedSeq, inf);
		trig = Impulse.kr(0.2 + (1 - density));
		newSpeed = Demand.kr(trig, 0, seq);
		
	 	signal = Resonz.ar(Array.fill(8, 
	 	{
			DynKlank.ar(`[[freq1, freq2, freq3, freq4], nil, [3.2, 2.8, 4.6, 2.4] * (0.01 + density)], 
			(Dust.ar((newSpeed*2)) * entropy) + (Impulse.ar(newSpeed) * (1-entropy)))
		}), freq1 * ([7, 11, 13, 17, 19, 23, 29, 31] * (0.01 + surface)), 0.1).sum;
	 	
	 	addImpulse = (1 - entropy) * (Impulse.ar(newSpeed) + Impulse.ar(newSpeed * 4) + Impulse.ar(newSpeed * 8));
	 	addDust = entropy * (Dust.ar(newSpeed*2) + Dust.ar(newSpeed * 8) + Dust.ar(newSpeed * 16));
	 	
	 	signal = signal + ((addImpulse + addDust) * surface);
	 	signal = (Clipper8.ar(signal) * surface) + (signal * (1 - surface)); 
		signal = LeakDC.ar(signal);
		signal = signal * env;
		signal = signal * amp;
		signal = Pan2.ar(signal, pan);
		signal = Limiter.ar(signal, 0.9, 0.01);
		Out.ar(out, signal);
		
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add; 
		
		SynthDef(\ethylVerb,{arg in = 3, out = 0, mix=0.5, fftsize = 1, specBufNum, roomsize=80, revtime=4.85, damping=0.41, inputbw=0.19, spread = 15, drylevel=(-3), earlylevel=(-9), 		taillevel=(-11);
		var mix2 = mix * 0.15; 
		var signal = InFeedback.ar(in, 2);
		signal = (PartConv.ar(signal, fftsize, specBufNum) * mix) + (signal * (mix - 1));
			
		signal = GVerb.ar(
		signal,
		roomsize, 
		revtime, 
		damping, 
		inputbw, 
		spread, 
		drylevel.dbamp,
		earlylevel.dbamp, 
		taillevel.dbamp,
		roomsize, 0.3) * mix2 + (signal * (1-mix2));
		signal = BPeakEQ.ar(signal, 80, 2, (-4));
		ReplaceOut.ar(out, Limiter.ar(signal, 0.99, 0.05));
		
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
		wrap = Bwrap.new(\ethyl, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Ethyl start'.postln);
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
		irspectrum.free;
	}
	
	update {arg updateAll=0;
	var newFrequency1, newFrequency2, newFrequency3, newFrequency4, newFrequency5, newFrequency6, newFrequency7, newFrequency8;
	var newAttack, newRelease, newSustain, newAmp, newSpeed;
	
	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.entropy != preControl.entropy) || (control.density != preControl.density), 
	{	
		newFrequency1 = Env.new([20, 40, 80, 200, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		newFrequency2 = newFrequency1 + (control.density * 25 * 1);
		newFrequency3 = newFrequency1 + (control.density * 25 * 2);
		newFrequency4 = newFrequency1 + (control.density * 25 * 3);
		wrap.set(\freq1, newFrequency1);
		wrap.set(\freq2, newFrequency2);
		wrap.set(\freq3, newFrequency3);
		wrap.set(\freq4, newFrequency4);
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{	
		wrap.set(\density, Env.new([0.0, 0.2, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
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
		newSustain = duration - (newAttack + newRelease);
		wrap.set(\sus, newSustain);
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		newAmp = Env.new([0.0, 0.05, 0.4, 0.75, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		wrap.set(\amp, newAmp);
	});
	
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{
		newSpeed = Env.new([0.02, 0.5, 1, 8, 20],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		wrap.set(\speed, newSpeed);
	});
		
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\mix, Env.new([0.0, 0.1, 0.2, 0.3, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		wrap.set(\surface, control.surface);
	});
	
	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\pan, control.location.linlin(0,1,-1,1));
	});
	
	if(updateAll == 1 || (control.position != preControl.position), 
	{
		wrap.set(\speedSeq, BUtils.limitArrayByPositionSameSize(control.position, durations));
	});
	
	preControl.copy(this.control);
	
	}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BGenEthyl: Convoluted resonant impulses";
	}
}
