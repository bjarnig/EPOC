//
//BPya : Bseq
//{
//	var <>outBus, <>sounds;
//
//	*new { |duration=inf, direction=0, speed=0.5, density=0.5, frequency=0.5, entropy=0.5, amplitude=0.5, surface=0.5, location=0.5, attack=0.5, release=0.5, outBus=0, sounds|
//		^super.newCopyArgs(nil, duration, direction, speed, density, frequency, entropy, amplitude, surface, location, attack, release, outBus, sounds).init();
//	}
//
//	init {
//		this.setDescription;
//	}
//
//	*loadSynthDefs {
//
//	 	SynthDef(\ya,
//		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
//		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
//		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
//		signal = HPF.ar(signal, 70);
//		signal = signal * env;
//		OffsetOut.ar(bus, Pan2.ar(signal, pan));
//		}).add;
//	}
//
//	play {
//
//		var pat1, pat2, halfSounds, bus1, reverb, durations, pitch;
//
//		halfSounds = sounds.size/2;
//		durations = List.new();
//		pitch = frequency.linexp(0.0, 1.0, 0.1, 10);
//		('pitch: ' ++ pitch).postln;
//		speed = speed.linexp(0.0, 1.0, 0.1, 1.5);
//		('pitch: ' ++ pitch).postln;
//
//		while( {durations.sum < duration },
//		{
//			durations.addAll(Array.fill(3 + 8.rand, { (((1-speed) / (4 * speed)) + (1-speed).rand * 0.7) }));
//			durations.add((4 + 4.rand) * (1 - density));
//		});
//
//		('durations: ' ++ durations).postln;
//
//		bus1 = Bus.audio(Server.local, 2);
//		reverb = BEmami.new([\in, bus1, \out, outBus, \delaytime, 0.8, \decaytime, 4 * location, \dryWet, 0.6 * location * surface, \hipass, 19200, \lopass, 100]);
//		reverb.play;
//
//		pat1 = Pbind(
//		\instrument, 'ya',
//		\dur, Pseq( durations, inf),
//		\grdur, density * 1.5,
//		\buf, Pindex(sounds, Pbeta(0, halfSounds), inf),
//		\offset, 0,
//		\amp, amplitude,
//		\rate, Pbeta(pitch*0.8, pitch*1.1, 0.6, 0.4, inf),
//		\pan, Pbeta(-0.3, 0.3, 0.6, 0.4),
//		\bus, bus1
//		);
//
//		pat2 = Pbind(
//		\instrument, 'ya',
//		\dur, Pseq(durations, inf),
//		\grdur, density * 1.5,
//		\buf, Pindex(sounds, Pbeta(halfSounds, sounds.size), inf),
//		\offset, 0,
//		\amp, amplitude,
//		\rate, Pbeta(pitch*0.8, pitch*1.2, 0.6, 0.4, inf),
//		\pan, Pbeta(-0.3, 0.3, 0.4, 0.6),
//		\bus, bus1
//		);
//
//		this.playDuration(Ppar([pat1, pat2], 1));
//
//	}
//
//		setDescription {
//		description = "";
//	}
//}