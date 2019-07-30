
BLPat3 : BSeq
{
	var <>durations, <>soundsA, <>durationFactor, buffersA, currentPattern, reverb, effectBus, distortion, distBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, 
	proxyBuf, proxyDist, proxyOffset, <>isPlaying, preControl;
	
	*new { |id=0, description="BLPat3", duration=10, control, outBus=0, durations, soundsA, durationFactor=1.1, load=1|
		
		^super.newCopyArgs(id, description, duration, control, outBus, durations, soundsA, durationFactor).init(load); 
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new; 
		
		if(load > 0, {
		
		if(this.durations.isNil, {this.durations = [0.12, 0.1, 0.08, 0.04, 0.02]});
		if(this.durationFactor.isNil, {this.durationFactor = 1.4});
		buffersA = this.readFiles(soundsA).value;
		
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLPat3Reso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
		
		distBus = Bus.audio(Server.local, 2);
		distortion = Bwrap.new(\bLPat3Crusher, [\inBus, distBus, \outBus, effectBus]);
		distortion.play; });
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue; });
	if(paramName == \soundsA, {soundsA = paramValue; });
	if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	readFiles {arg path; 
		var files, buffers;
		if(path.isNil, {path = BConstants.monoImpulses});
		files = SoundFile.collect(path);
		buffers = files.collect { |sf| var b;
		b = Buffer.read(Server.local, sf.path);};
		buffers;
 		^buffers;
	}
	
	*loadSynthDefs {
		
		SynthDef(\bLPat3, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1, buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 0);
		signal = signal * env * grAmp;
		signal =  Compander.ar(signal * 2, signal, 1, 1, 0.05, 0.01, 0.01);
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;  	
		
		SynthDef(\bLPat3Crusher, {| inBus=0, outBus=0, samplerate=44100, bitsize=16, fxlevel=0.5, level=0.5, pitchShift=1.0| 
		var fx, sig, bitRedux; 
		sig = InFeedback.ar(inBus, 2);
		bitRedux = (Clipper8.ar(sig) * 0.3) +  (Clipper8.ar(HPF.ar(sig, 4000), -0.4, 0.4) * 0.8); 
		bitRedux = LeastChange.ar(bitRedux, PitchShift.ar(sig, 0.01, pitchShift, 0, 0.004));
		bitRedux = bitRedux.tanh;  
		Out.ar(outBus, (bitRedux*fxlevel) + (sig * level))
		}).add; 
		
		SynthDef(\bLPat3Reso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, predelay=0.048,combdecay=4, allpassdecay=14|
		var sig, y, z;
		sig = InFeedback.ar(inBus, 2); 
		z = DelayN.ar(sig, 0.1, predelay);
		y = Mix.ar(Array.fill(2,{ CombL.ar(z, 0.2, rrand(0.05, 0.2), combdecay).distort })); 
		6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
		Out.ar(outBus, ((sig * (1 - dryWet)) + (y * (dryWet*0.5))) * amp);
		}).add;
	}
	
	update {
		
		var pat, dur, grDur, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax;
		var decayMin, decayMax, offsetPattern, ratePattern, panPattern, durs, sndSel, durFrom, durTo, offset;
		
		if(control.position < 0.01, {control.position = 0.01});
				
		grDur = Env.new([0.05, 0.1, 0.3, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = Env.new([0.1, 0.2, 0.4, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grRel = Env.new([0.1, 0.2, 0.4, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		amp = Env.new([0.0, 0.2, 0.5, 0.75, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		
		dur = durations * Env.new([8.1, 2.1, 1.0, 0.25, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed); 
		rate = Env.new([0.25, 0.5, 1, 1.5, 4],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		pan = control.location.linlin(0, 1, -1, 1);
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		
		if(pan > 0.5, {panPattern = Pwhite(pan, (pan * 1.4).min(1.0))}, {panPattern = Pwhite((pan * 0.6).max(-1.0), pan)});
		
		durFrom = Env.new(this.durations,[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		durTo = Env.new(this.durations * (this.durationFactor + control.entropy),[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		durs =  Pseq([Pwhite(durFrom, durTo, rrand(52,78)), Pwhite(this.durations[0] * 5, this.durations[1] * 15, rrand(1, 6))], inf);
		sndSel = Prand(buffersA, inf); 
 		
		readFrom = control.position; 
		readTo = (readFrom + 0.05 + control.entropy).min(1.0);
		offset = Pwhite(readFrom, readTo, inf); 
		
		this.rev;
	   	
	   	if(this.isPlaying > 0,
		{			
			if(control.density != preControl.density, {proxyGrDur.source = grDur; proxyAtk.source = grAtk; proxyRel.source = grRel; proxyBuf.source = sndSel});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePattern});
			if(control.location != preControl.location, {proxyPan.source = panPattern});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = amp});
			if(control.speed != preControl.speed, {proxyDur.source = durs});
			if(control.entropy != preControl.entropy, {proxyBuf.source = sndSel; proxyDur.source = durs});
			if(control.position != preControl.position, {proxyBuf.source = sndSel});
			if(control.position != preControl.position || control.entropy != preControl.entropy, {proxyOffset.source = offset});
		}, 
		{
			('BLPat3 start'.postln);
			proxyDur = PatternProxy(durs);
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(sndSel);
			proxyOffset = PatternProxy(offset);
			
			currentPattern.stop;
			pat = Pbind(
			\instrument, 'bLPat3',
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\buf, proxyBuf,
			\offset, proxyOffset,
			\rate, proxyRate,
			\pan, proxyPan, 
			\grAmp, proxyGrAmp,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, distBus;
		);
		
		this.playDuration(pat);
		this.isPlaying = 1;
		
		});
		
		preControl.copy(this.control);
	}
	
	rev {
		
		if(control.color != preControl.color, {reverb.set(\dryWet, Env.new([0.0, 0.002, 0.02, 0.1, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.color))}); 
		if(control.surface != preControl.surface, { 
		distortion.set(\fxlevel, Env.new([0.0, 0.02, 0.2, 0.65, 0.8],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\pitchShift, Env.new([1.0, 0.5, 0.1, 1.5, 2.0],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\level, Env.new([1.0, 0.98, 0.6, 0.35, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.surface)) 
		
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
	
		this.freeEffect;
		buffersA.do({arg item; item.free;});
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
		 if(distBus.index.isNil.not, {distBus.free}); 
		 if(distortion.synth.isPlaying, {distortion.stop});
	}
	
	setDescription {
		description = "BLPat3";
	}
}