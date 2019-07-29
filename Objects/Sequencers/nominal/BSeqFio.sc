
BSeqFio : BSeq
{
	var <>sounds, <>durations, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyOffset, proxyRate, proxyPan, proxyGrAmp, proxyBuf, proxyDistort,
	proxyLpf, lastFrequency, lastDur, isPlaying, grAtk, grRel;
	
	*new { |id=0, description, duration, control, outBus=0, sounds, durations, grAtk=0.01, grRel=0.01|
		
		^super.newCopyArgs(id, description, duration, control, outBus, sounds, durations, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, grAtk, grRel).init(); 
	}
	
	init {
		
		this.setDescription;
		isPlaying = 0;
	}
	
	*loadSynthDefs {
				
		SynthDef(\fio, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp=1.0, atk=0.001, rel=0.001, rate=1, offset=0, buf=0, lop=20000, hip=20, distort=0.0, lpf=1.0|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1, buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		var distorted = (Compander.ar(signal.softclip, signal.softclip, 1, 0, 1 )) * distort * 0.25;
		var filtered = (RLPF.ar(signal, 18000 * lpf, 1) * (1.0-distort)); 
		// signal = distorted + filtered; 
		signal = signal * env * grAmp * amp;
		// signal = LeakDC.ar(signal);
		// signal = HPF.ar(signal, 80);
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
		
		SynthDef(\fioReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.001|
		var freqs, ringtimes, input, signal; 
		freqs = Control.names([\freqs]).kr([60, 180, 400, 800, 1400, 2000, 4000, 8000, 160, 280, 500, 1800, 2400, 2500, 4500, 10000]);
		ringtimes = Control.names([\ringtimes]).kr([0.25, 0.3, 0.1, 0.4, 0.8, 1.0, 0.25, 0.7, 0.25, 0.3, 0.1, 0.4, 0.8, 1.0, 0.25, 0.7]);
		input = In.ar(inBus, 2); 
		signal = DynKlank.ar(`[ 
		freqs,		
		Array.rand(16, 0.5, 0.8),
		ringtimes 			
		], input); 
		signal = signal * 0.001 * amp * dryWet;
		signal = signal + (input * (1 - dryWet));
		Out.ar(outBus, Pan2.ar(signal, 0));
		}).add;
	
	}	
	
	update {
		
		var pat, dur, grDur, readFrom, readTo, rate, atk, sus, rel, amp, pan, reverb, bus1;
		var decayMin, decayMax, offsetPattern, ratePattern, panPattern, newDurations, newDistort, newLpf, n=0;
				
		grDur = grAtk * 2;
		amp = control.amplitude.linlin(0.0, 1.0, 0.0, 6.0);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);

		rate = control.frequency.linlin(0.0, 1.0, 0.25, 2);
		pan = control.location * 0.5;
		readFrom = 0.1 * control.location;
		readTo = readFrom + control.entropy;
		offsetPattern = Pwhite(readFrom, readTo);
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		panPattern = Pwhite((pan * -1), pan);
		
		newDurations = Array.new(durations.size);
		durations.do{arg item;
		newDurations.add(item + rrand(0.0, control.entropy * 0.1));
		n = n + 1;
		};
		
		dur = newDurations * (0.01 + ((1 - control.speed) * 2)); 
		
		if(readTo > 1.0, {readTo = readFrom - control.entropy});
		if(readTo < 0.0, {readTo = 0.0});
		
		if(control.surface < 0.5, {newLpf = control.surface; newDistort=0}, {newLpf = 1.0; newDistort=control.surface - 0.5});
		if(control.surface == 0.5, {newDistort=0; newLpf=1 });
		
		this.rev;
		lastFrequency = control.frequency;
		
	   	if(isPlaying > 0,
		{
			proxyDistort.source = newDistort;
			proxyLpf.source = newLpf; 
			proxyGrDur.source = grDur;
			proxyAtk.source = grAtk; 
			proxyRel.source = grRel;
			proxyOffset.source = offsetPattern;
			proxyRate.source = ratePattern;
			proxyPan.source = panPattern;
			proxyGrAmp.source = amp;
			proxyBuf.source = Pindex(sounds, Pwhite(sounds.size * control.location * (1- control.density), sounds.size * control.location), inf);
			if(lastDur != dur, {proxyDur.source = Pseq(dur, inf)});
		}, 
		{
			proxyDistort = PatternProxy(newDistort);
			proxyLpf = PatternProxy(newLpf);
			proxyDur = PatternProxy(Pseq(dur, inf));
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyOffset = PatternProxy(offsetPattern);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(Pindex(sounds, Pwhite(sounds.size * control.location * (1- control.density), sounds.size  * control.location), inf));
			
			currentPattern.stop;
			pat = Pbind(
			\instrument, 'fio',
			\delta, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\buf, proxyBuf,
			\offset, 0,
			\rate, proxyRate, 
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\pan, proxyPan, 
			\grAmp, proxyGrAmp,
			\distort, proxyDistort,
			\lpf, proxyLpf,
			\bus, effectBus
		);
		
		lastDur = dur;
		this.playDuration(pat);
		isPlaying = 1;
		
		});
	}
	
	rev {
		reverb.set(\dryWet, control.color);
		if(control.frequency != lastFrequency, {
		reverb.setn(\freqs, Array.rand(16, 40, (8000 * control.frequency).ceil));
		reverb.setn(\ringtimes, Array.rand(16, 0.5, 4));		
		});
	}
	
	play {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\fioReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
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
			this.freeEffect;
			isPlaying = 0;
		})
		}.play;
	}

	stop {
		
		Routine 
		{
			1.do({	
			currentPattern.stop;
			3.wait;
			this.freeEffect;
			isPlaying = 0;
		})
		}.play;
	}
	
	setDescription {
		description = "";
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	

}