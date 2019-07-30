
BUtils
{
	*limit {arg val, from=0.0, to=1.0;

	if(val > to, {val = to});
	if(val < from, {val = from});

	^val;

	}

	*limitArrayByPosition {arg pos=0.1, arr=[1,2,3,4,5,6,7,8,9,10];
	var size, offset;

	size = arr.size;
	offset = (size * pos).floor.asInt;
	^arr.copyRange(offset, offset + (size - offset));
	}

	*limitArrayByPositionSameSize {arg pos=0.1, arr=[1,2,3,4,5,6,7,8,9,10];
	var size, offset, output, diff, count, index;

	size = arr.size;
	offset = (size * pos).floor.asInt;
	output = arr.copyRange(offset, offset + (size - offset));
	diff = size - output.size;
	index = output.size;
	output = output.extend(size, output[0]);
	count = 0;

	diff.do({

	output[index] = output[count];
	count = count + 1;
	index = index + 1;

	if(count > (output.size), {count = 0});

	});

	^output;

	}

	*limitArrayLastVals {arg count=2, arr=[1,2,3,4,5,6,7,8,9,10];
		^arr.copyRange(arr.size - count, arr.size);
	}

	*shrinkArrayForEnv {arg arr=[1,2,3,4,5,6,7,8,9,10];
		^arr.copyRange(0, arr.size - 2);
	}

	*materialDir {
		^Platform.userExtensionDir ++ "/EPOC/Material/";
	}

	*loadLiveObjects {

		BLSynth1.loadSynthDefs.value;
		BLSynth2.loadSynthDefs.value;
		BLSynth3.loadSynthDefs.value;
		BLSynth4.loadSynthDefs.value;
		BLSynth5.loadSynthDefs.value;
		BLSynth6.loadSynthDefs.value;
		BLSynth7.loadSynthDefs.value;
		BLSynth8.loadSynthDefs.value;
		BLSynth9.loadSynthDefs.value;

		BLGest1.loadSynthDefs.value;
		BLGest2.loadSynthDefs.value;
		BLGest3.loadSynthDefs.value;
		BLGest4.loadSynthDefs.value;
		BLGest5.loadSynthDefs.value;
		BLGest6.loadSynthDefs.value;
		BLGest7.loadSynthDefs.value;
		BLGest8.loadSynthDefs.value;
		BLGest9.loadSynthDefs.value;

		BLText1.loadSynthDefs.value;
		BLText2.loadSynthDefs.value;

		BLPat1.loadSynthDefs.value;
		BLPat2.loadSynthDefs.value;
		BLPat3.loadSynthDefs.value;
		BLPat4.loadSynthDefs.value;
		BLPat5.loadSynthDefs.value;
		BLPat6.loadSynthDefs.value;
		BLPat7.loadSynthDefs.value;
		BLPat8.loadSynthDefs.value;

		'## Loading live objects completed ##'.postln;
	}
}
