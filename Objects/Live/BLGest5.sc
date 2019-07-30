BLGest5 : BSeq
{
	var <>durations, <>sound, startPos, jitAmt, jitFreq, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, proxyOffset, proxyOffsetJitter, proxySurface, proxyStretch, <>isPlaying, buf, preControl;
	
	*new { |id=0, description="BLGest5", duration=10, control, outBus=0, durations, sound, startPos=0, jitAmt=0.0, jitFreq=1, load=1|
		
		^super.newCopyArgs(id, description, duration, control, outBus, durations, sound, startPos, jitAmt, jitFreq).init(load); 
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new; 
		
		if(load > 0, {
		if(durations.isNil, {this.durations = [0.5, 0.5]});
		if(sound.isNil, {this.sound = BConstants.stereoSnd});
		buf = Buffer.read(Server.local, sound);
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLGest5Reso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play; });
	}
	
	setParam {|paramName, paramValue| 
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \sound, {sound = paramValue});
		if(paramName == \durations, {durations = paramValue});
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \jitAmt, {jitAmt = paramValue}); 
		if(paramName == \jitFreq, {jitFreq = paramValue}); 
		if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	*loadSynthDefs {
		
		SynthDef(\bLGest5, 
		{|bus = 0, amp=1, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0, surface=0.5, offsetJitter=1, pan=0.0, stretch=0.1|
		var distortIn, distort, amount, amCoef, env, signal, distAmt=0.0, nonDistAmt=1.0, eqDb=10;
		env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		signal = Pan2.ar(BufRd.ar(2, buf, Phasor.ar(Impulse.ar(stretch), rate, offset * (offsetJitter * BufFrames.ir(buf)),  BufFrames.ir(buf), 1)), pan); 
		signal = HPF.ar(signal, 60);
		signal = signal * env * grAmp;
		distortIn = HPF.ar(signal, 800);
		distAmt = (surface - 0.5).max(0.0);
		nonDistAmt = (1.0 - distAmt) - 0.25;
		amount = 0.99;
		amCoef= 2*amount/(1-amount);
		distort = MidEQ.ar(LPF.ar((1+amCoef)*distortIn/(1+(amCoef*distortIn.abs)), [3800, 3900])*0.5, 120, 0.7, 8) * 0.85;
		signal = (distort * distAmt) + (signal * nonDistAmt);
		eqDb = 15 * surface;
		signal = MidEQ.ar(signal, 100, 8, eqDb, mul: 0.1) + MidEQ.ar(signal, 1000, 8, eqDb * (-1), mul: 0.2) + MidEQ.ar(signal, 8000, 2, eqDb, mul: 0.1);
		OffsetOut.ar(bus, signal);
		}).add; 
		
//		SynthDef(\bLGest5Reso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=4.85, damping=0.21, inputbw=0.19, 
//		earlylevel=(-12), taillevel=(-11), spread = 15|
//		var input, signal; 
//		input = In.ar(inBus, 2); 
//		
//		signal = (GVerb.ar( 
//		input,
//		roomsize, 
//		revtime, 
//		damping, 
//		inputbw, 
//		spread, 
//		0,
//		earlylevel.dbamp, 
//		taillevel.dbamp,
//		roomsize, dryWet) * 1.3) + (input*(1-dryWet));
//		
//		Out.ar(outBus, signal);
//		}).add;

		SynthDef(\bLGest5Reso,{| outBus = 0, inBus=2, amp=1, dryWet=0.01, decay=1.0, density=0.25, fund=60, randMin=1.0, randMax=1.0|
Ê Ê Ê Ê Êvar input	, combi1, combi2, output, reso, effect;
		input = In.ar(inBus, 2);
		reso = input;
		reso = DynKlank.ar(`[[fund,fund*2,fund*3,fund*4,fund*5,fund*6,fund*7,fund*8,fund*9,fund*10],
		[0.05,0.02,0.04,0.06,0.11,0.01,0.15,0.03,0.15,0.02] * rrand(randMin, randMax), [0.15, 0.12, 0.14, 0.16, 0.11, 0.12, 0.15, 0.13, 0.15, 0.12] * decay], input); 
		combi1 = LPF.ar(CombC.ar(reso, 1, 1.0 * density, 2 * density), 1000) * 0.16;
Ê  Ê Ê Ê	combi2 = LPF.ar(CombC.ar(reso, 1, 0.9 * density, 4 * density), 500) * 0.15;
		effect = (combi1 + combi2 + HPF.ar(reso, 80)) * 0.5;
		output = ((effect * 0.7) * dryWet) + (input * (1 - dryWet));
Ê Ê Ê Ê	Out.ar(outBus, output * amp);

	}).add;

	}
	
	update {
		
		var pat, dur, grDur, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1;
		var decayMin, decayMax, ratePattern, seqRep, surface, entropy, speedEnvVal, offset, offsetDur, offsetJitter, stretch;
		
		grDur = Env.new([0.01, 0.2, 2, 4, 6],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = Env.new([0.0, 0.1, 0.6, 1.2, 10],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		speedEnvVal = Env.new([12, 2, 1, 0.25, 0.08],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		dur = durations * speedEnvVal; 
		rate = Env.new([0.125, 0.5, 1, 2, 8],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		surface = control.surface;
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		pan = Pwhite(Env.new([-0.9, -0.6, -0.1, 0.3, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.location), Env.new([-0.6, -0.1, 0.1, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.location)); 
		if(dur.sum >= duration, {seqRep = 1}, {seqRep = inf});
		entropy = Pseq(dur, seqRep) * Pbeta(rrand(1 - control.entropy, 1), 1.0, 0.8, 0.2); 
		offsetDur = (duration*0.5) * ((1 - control.speed) * 2);
		offset = Pseg( Pseq([control.position, 1],inf), Pseq([offsetDur, offsetDur],inf), \linear);
		offsetJitter = Pwhite(1-control.entropy, 1);
		stretch = Env.new([0.05, 0.2, 1, 1.5, 2],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		
		
		
	if(control.frequency != preControl.frequency, 
	{
		reverb.set(\fund, Env.new([25, 47, 63, 82, 128],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		// reverb.set(\fund, Env.new([80, 120, 1140, 2000, 4000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});
	
	if(control.entropy != preControl.entropy, 
	{	
		reverb.set(\randMin, Env.new([1.0, 0.9, 0.8, 0.6, 0.4],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		reverb.set(\randMax, Env.new([1.0, 1.1, 1.2, 1.4, 1.6],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});
	
	if(control.density != preControl.density, 
	{	
		reverb.set(\density, Env.new([0.01, 0.2, 1, 2, 4],[0.25, 0.25, 0.25, 0.25]).at(control.density));
		reverb.set(\decay, Env.new([0.001, 0.002, 0.04, 0.08, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});
		
	if(control.color != preControl.color, 
	{
		reverb.set(\mix, Env.new([0.0, 0.05, 0.1, 0.25, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});
	
	this.rev;
		
	   	if(this.isPlaying > 0,
		{	
			if(control.density != preControl.density, {proxyGrDur.source = grDur});
			if(control.speed != preControl.speed, {proxyStretch.source = stretch});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePattern});
			if(control.location != preControl.location, {proxyPan.source = pan});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = amp; });  
			if(control.surface != preControl.surface, {proxySurface.source = surface});
			if(control.entropy != preControl.entropy || control.speed != preControl.speed || control.position != preControl.position, 
			{proxyDur.source=entropy; proxyOffset.source=offset; proxyOffsetJitter.source=offsetJitter});
		}, 
		{
			('BLGest5 start'.postln);
			proxyDur = PatternProxy(entropy); 
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(pan);
			proxyGrAmp = PatternProxy(amp);
			proxySurface = PatternProxy(surface);
			proxyStretch = PatternProxy(stretch);
			proxyOffset = PatternProxy(offset); 
			proxyOffsetJitter = PatternProxy(offsetJitter); 
			
			currentPattern.stop;
			
			pat = Pbind(
			\instrument, 'bLGest5',
			\delta, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\buf, buf,
			\offset, proxyOffset,
			\offsetJitter, proxyOffsetJitter, 
			\rate, proxyRate,
			\pan, proxyPan, 
			\grAmp, proxyGrAmp,
			\surface, proxySurface,
			\jitAmt, jitAmt, 
			\jitFreq, jitFreq,
			\stretch, proxyStretch, 
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, effectBus
		);
		
		this.playDuration(pat);
		this.isPlaying = 1;
		
		});
		
		preControl.copy(this.control); 
	}
	
	rev {
		if(control.color != preControl.color, 
		{
			reverb.set(\dryWet, control.color); 
		});
		if(control.location != preControl.location, 
		{
			reverb.set(\spread, Env.new([2, 4, 8, 15, 50],[0.25, 0.25, 0.25, 0.25]).at(control.location))
		}); 
	}
	
	play {
		this.update.value;
	}
	
	playDuration 
	{|pat|
		
		Routine 
		{
			1.do({	
		     currentPattern = pat.play;
			duration.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	stop {arg release=0;
		
		Routine 
		{
			1.do({	
			
			if(release > 0, { 
			proxyGrAmp.source = Pif(Ptime(inf) <= (release), Env.new([control.amplitude, 0],[release], [-2]));
			});
			
			release.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}
	
	dispose {
	
		if(this.isPlaying == 1, {this.stop.value});
		this.freeEffect;
		buf.free;
		this.isPlaying = 0;
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
	}
	
	setDescription {
		description = "BLGest5";
	}
}