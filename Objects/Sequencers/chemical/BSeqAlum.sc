BSeqAlum : BSeq
{
	var <>durations, <>sound, startPos, jitAmt, jitFreq, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, proxyOffset, proxyOffsetJitter, proxySurface, <>isPlaying, buf, preControl;
	
	*new { |id=0, description="BSeqAlum", duration=10, control, outBus=0, durations, sound, startPos=0, jitAmt=0.0, jitFreq=1, load=1|
		
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
		reverb = Bwrap.new(\alumReso, [\inBus, effectBus, \outBus, outBus]);
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
		
		SynthDef(\alum, 
		{|bus = 0, amp=1, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0, surface=0.5, offsetJitter=1, pan=0.0|
		var distortIn, distort, amount, amCoef, env, signal, distAmt=0.0, nonDistAmt=1.0, eqDb=10;
		env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		signal = Pan2.ar(PlayBuf.ar(1,buf, rate * BufRateScale.ir(buf), 1, offset * (offsetJitter * BufFrames.ir(buf)), 1), pan);
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
		
		SynthDef(\alumReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=4.85, damping=0.21, inputbw=0.19, 
		earlylevel=(-12), taillevel=(-11), spread = 15|
		var input, signal; 
		input = In.ar(inBus, 2); 
		
		signal = (GVerb.ar( 
		input,
		roomsize, 
		revtime, 
		damping, 
		inputbw, 
		spread, 
		0,
		earlylevel.dbamp, 
		taillevel.dbamp,
		roomsize, dryWet) * 1.3) + (input*(1-dryWet));
		
		Out.ar(outBus, signal);
		}).add;
	}
	
	update {
		
		var pat, dur, grDur, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1;
		var decayMin, decayMax, ratePattern, seqRep, surface, entropy, speedEnvVal, offset, offsetDur, offsetJitter;
		
		grDur = Env.new([0.01, 0.2, 2, 4, 6],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = Env.new([0.0, 0.1, 0.6, 1.2, 10],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		speedEnvVal = Env.new([12, 2, 1, 0.25, 0.08],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		dur = durations * speedEnvVal; 
		rate = control.frequency + 0.5;
		surface = control.surface;
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		pan = Pwhite(Env.new([-0.9, -0.6, -0.1, 0.3, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.location), Env.new([-0.6, -0.1, 0.1, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.location)); 
		if(dur.sum >= duration, {seqRep = 1}, {seqRep = inf});
		entropy = Pseq(dur, seqRep) * Pbeta(rrand(1 - control.entropy, 1), 1.0, 0.8, 0.2); 
		offsetDur = (duration*0.5) * ((1 - control.speed) * 2);
		offset = Pseg( Pseq([control.position, 1],inf), Pseq([offsetDur, offsetDur],inf), \linear);
		offsetJitter = Pwhite(1-control.entropy, 1);
		
		this.rev; 
		
	   	if(this.isPlaying > 0,
		{	
			if(control.density != preControl.density, {proxyGrDur.source = grDur});
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
			('Alum start'.postln);
			proxyDur = PatternProxy(entropy); 
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(pan);
			proxyGrAmp = PatternProxy(amp);
			proxySurface = PatternProxy(surface);
			proxyOffset = PatternProxy(offset); 
			proxyOffsetJitter = PatternProxy(offsetJitter); 
			
			currentPattern.stop;
			
			pat = Pbind(
			\instrument, 'alum',
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
		description = "BSeqAlum";
	}
}