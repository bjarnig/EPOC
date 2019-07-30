EpocEditor
{

*view {arg objects, savedObjects, header="OBJECTS EDITOR", posX=600, posY=200, color=1;
	var control, spec, window, xSlider, xSliderOffset, yOffset, xText, xTextOffset, yButton, yNumberBoxUpper, yNumberBoxLower, xSliderOffsetLower;
	var yFaderUpper, yFaderLower, yTextUpper, yTextLower;
	var updateObjects, updateGui;
	var xButton, xButtonOffset, buttonWidth, yLearnOffset, yButtonRand, yButtonLearnFader, ybuttonLearnTrigger, yPop, yRandom, yRandomText;
	var sliderGUIColor, buttonColor, buttonColorText, buttonColorOnText, buttonColorOn, colorText, knobColor, knobColorMore, dropDownColor, dropDownBackColor;
	var release=0.25;
	var midiVelocityLower = 0.5;
	var midiVelocityUpper = 0.75;
	var popObj1,popObj2,popObj3,popObj4,popObj5,popObj6,popObj7,popObj8;
	var nbSpeed ,nbDensity ,nbFrequency ,nbEntropy,nbAmplitude  ,nbColor  ,nbSurface,nbLocation ,nbPosition,nbAmp1,nbAmp2,nbAmp3,nbAmp4,nbAmp5,nbAmp6,nbAmp7;
	var nbAmp8,nbRandFrom ,nbRandTo ,nbName1 ,nbName2  ,nbName3 ,nbName4 ,nbName5 ,nbName6 ,nbName7,nbName8;
	var buttonSave1, buttonSave2, buttonSave3, buttonSave4, buttonSave5, buttonSave6, buttonSave7, buttonSave8;
	var buttonLearn1, buttonLearn2, buttonLearn3, buttonLearn4, buttonLearn5, buttonLearn6, buttonLearn7, buttonLearn8, buttonLearn9;
	var buttonLearnTrigger1, buttonLearnTrigger2, buttonLearnTrigger3, buttonLearnTrigger4, buttonLearnTrigger5, buttonLearnTrigger6;
	var buttonLearnTrigger7, buttonLearnTrigger8, buttonLearnTrigger9, buttonValues;
	var buttonRand1, buttonRand2, buttonRand3, buttonRand4, buttonRand5, buttonRand6, buttonRand7, buttonRand8, buttonRand9;
	var buttonDispose1, buttonDispose2, buttonDispose3, buttonDispose4, buttonDispose5, buttonDispose6, buttonDispose7, buttonDispose8;
	var midiButton1, midiButton2, midiButton3, midiButton4, midiButton5, midiButton6, midiButton7, midiButton8;
	var sliderSpeed, sliderDensity, sliderFrequency, sliderEntropy, sliderAmplitude, sliderColor, sliderSurface, sliderLocation, sliderPosition;
	var buttonObj1, buttonObj2, buttonObj3, buttonObj4, buttonObj5, buttonObj6, buttonObj7, buttonObj8;
	var buttonObj9,buttonObj10,buttonRandom,buttonRandomExp,buttonRandomBilin,buttonRandomSum,buttonRandom1,buttonRandom2,buttonRandom3;
	var obj1, obj2, obj3, obj4, obj5, obj6, obj7, obj8;
	var item1, item2, item3, item4, item5, item6, item7, item8;
	var midiFader1, midiFader2, midiFader3, midiFader4, midiFader5, midiFader6, midiFader7, midiFader8, midiFader9;
	var obj1IsPlaying = 0, obj2IsPlaying = 0, obj3IsPlaying = 0, obj4IsPlaying = 0, obj5IsPlaying = 0, obj6IsPlaying = 0, obj7IsPlaying = 0, obj8IsPlaying = 0;
	var getNetworkItemNames, changePopUpObject, nbDuration, buttonSetDuration;
	var printItems;
	var defaultFont = Font("Monaco", 10);

	spec = ControlSpec(0.0,1.0,'linear');
	control = BControl.new;

	window = Window(header, Rect(posX,posY,660,430));
	window.background = Color.fromHexString("#1C0F13");
	window.front;

	window.drawFunc = { |v|
		Pen.fillColor = Color.white;
		Pen.strokeColor = Color.white;
		Pen.line(20@365, 640@365);
		Pen.fillStroke;
	};

	sliderGUIColor = Color.fromHexString("#6E7E85");
	knobColor = Color.fromHexString("#B7CECE");
	knobColorMore = Color.fromHexString("#E2E2E2");
	colorText = Color.new(1,1,1);
	buttonColor = Color.fromHexString("#BBBAC6");
	buttonColorText = Color.new(0.0, 0.0, 0.0);
	buttonColorOn =  Color.fromHexString("#B7CECE");
	buttonColorOnText = Color.new(1,1,1);
	dropDownColor = Color.fromHexString("#222222"); // B7CECE
	dropDownBackColor = Color.fromHexString("#B7CECE");
	xSlider = 20;
	xSliderOffset = 70;
	xSliderOffsetLower = 70;
	xText = 20;
	xTextOffset = 70;
	yLearnOffset = 50;
	yButtonRand = 150;
	yButtonLearnFader = yButtonRand + 30;
	yButton = 210;
	ybuttonLearnTrigger = yButton + 40;
	yPop = ybuttonLearnTrigger + 30;
	yNumberBoxUpper = 120;
	yFaderUpper = 30;
	yFaderLower = 240;
	yTextUpper = 10;
	yTextLower = 305;
	yNumberBoxLower = yTextLower + 25;
	yRandomText = 372;
	yRandom = yRandomText + 23;
	xButton = 20;
	xButtonOffset = 70;
	buttonWidth = 60;
	getNetworkItemNames = {arg list;
	var output = Array.new(list.size+1);
	output.add("None");
	list.do({arg item; output.add(item.name);});
	output;};

	printItems = {
	var items, printOutput, printEnvCount;

	items = List.new;
	items.add(item1);
	items.add(item2);
	items.add(item3);
	items.add(item4);
	items.add(item5);
	items.add(item6);
	items.add(item7);
	items.add(item8);

	items.do({arg item;

	if(item.notNil, {

		printOutput = '~sequencerObjects.add(BNetworkItem.new(' ++ item.preObject ++ ', ' ++ item.printParams ++ ', "' ++ item.name ++ '",';
		printOutput.postln;
		printOutput = item.object.control.text.value ++ ',';
		printOutput.postln;
		printOutput = ' ' ++ item.start.value ++ ', ' ++ item.duration.value  ++ ', [';
		printOutput.postln;
		printEnvCount = 1;

		item.envelopes.do({arg e;
			printOutput = 'BNetworkEnvelope.new(Env.new(' ++ e.envelope.levels.round(0.001) ++ ',' ++ e.envelope.times.round(0.001) ++ '), 0, "'++ e.parameter ++'")';
			if(printEnvCount < item.envelopes.size, {printOutput = printOutput ++ ','});
			printOutput.postln;
			printEnvCount = printEnvCount + 1;
		});

			printOutput = ']));';
			printOutput.postln;
			});
		});
	};

	// POP UP

	popObj1 = PopUpMenu(window,Rect(xSlider, yPop,buttonWidth, 20));
	popObj1.background_(dropDownBackColor);
	popObj1.stringColor = dropDownColor;
	popObj1.font = defaultFont;
	popObj1.items = getNetworkItemNames.value(objects);
	popObj1.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj1.notNil, {obj1.dispose});
	if(menu.value > 0, {
	item1 = objects[menu.value-1];
	item1.initObject;
	obj1 = item1.object;
	obj1.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj1.control).value; obj1.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj1.control = control});
	('## Set slot 1: ' ++ obj1.description).postln })};

	popObj2 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 1), yPop,buttonWidth, 20));
	popObj2.background_(dropDownBackColor);
	popObj2.stringColor = dropDownColor;
	popObj2.font = defaultFont;
	popObj2.items = getNetworkItemNames.value(objects);
	popObj2.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj2.notNil, {obj2.dispose});
	if(menu.value > 0, {
	item2 = objects[menu.value-1];
	item2.initObject;
	obj2 = item2.object;
	obj2.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj2.control).value; obj2.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj2.control = control});
	('## Set slot 2: ' ++ obj2.description).postln })};

	popObj3 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 2), yPop,buttonWidth, 20));
	popObj3.background_(dropDownBackColor);
	popObj3.stringColor = dropDownColor;
	popObj3.font = defaultFont;
	popObj3.items = getNetworkItemNames.value(objects);
	popObj3.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj3.notNil, {obj3.dispose});
	if(menu.value > 0, {
	item3 = objects[menu.value-1];
	item3.initObject;
	obj3 = item3.object;
	obj3.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj3.control).value; obj3.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj3.control = control});
	('## Set slot 3: ' ++ obj3.description).postln })};

	popObj4 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 3), yPop,buttonWidth, 20));
	popObj4.background_(dropDownBackColor);
	popObj4.stringColor = dropDownColor;
	popObj4.font = defaultFont;
	popObj4.items = getNetworkItemNames.value(objects);
	popObj4.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj4.notNil, {obj4.dispose});
	if(menu.value > 0, {
	item4 = objects[menu.value-1];
	item4.initObject;
	obj4 = item4.object;
	obj4.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj4.control).value; obj4.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj4.control = control});
	('## Set slot 2: ' ++ obj4.description).postln })};

	popObj5 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 4), yPop,buttonWidth, 20));
	popObj5.background_(dropDownBackColor);
	popObj5.stringColor = dropDownColor;
	popObj5.font = defaultFont;
	popObj5.items = getNetworkItemNames.value(objects);
	popObj5.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj5.notNil, {obj5.dispose});
	if(menu.value > 0, {
	item5 = objects[menu.value-1];
	item5.initObject;
	obj5 = item5.object;
	obj5.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj5.control).value; obj5.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj5.control = control});
	('## Set slot 3: ' ++ obj5.description).postln })};

	popObj6 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 5), yPop,buttonWidth, 20));
	popObj6.background_(dropDownBackColor);
	popObj6.stringColor = dropDownColor;
	popObj6.font = defaultFont;
	popObj6.items = getNetworkItemNames.value(objects);
	popObj6.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj6.notNil, {obj6.dispose});
	if(menu.value > 0, {
	item6 = objects[menu.value-1];
	item6.initObject;
	obj6 = item6.object;
	obj6.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj6.control).value; obj6.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj6.control = control});
	('## Set slot 4: ' ++ obj6.description).postln })};

	popObj7 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 6), yPop,buttonWidth, 20));
	popObj7.background_(dropDownBackColor);
	popObj7.stringColor = dropDownColor;
	popObj7.font = defaultFont;
	popObj7.items = getNetworkItemNames.value(objects);
	popObj7.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj7.notNil, {obj7.dispose});
	if(menu.value > 0, {
	item7 = objects[menu.value-1];
	item7.initObject;
	obj7 = item7.object;
	obj7.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj7.control).value; obj7.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj7.control = control});
	('## Set slot 5: ' ++ obj7.description).postln })};

	popObj8 = PopUpMenu(window,Rect(xSlider + (xSliderOffsetLower * 7), yPop,buttonWidth, 20));
	popObj8.background_(dropDownBackColor);
	popObj8.stringColor = dropDownColor;
	popObj8.font = defaultFont;
	popObj8.items = getNetworkItemNames.value(objects);
	popObj8.action = { arg menu;
	[menu.value, menu.item].postln;
	if(obj8.notNil, {obj8.dispose});
	if(menu.value > 0, {
	item8 = objects[menu.value-1];
	item8.initObject;
	obj8 = item8.object;
	obj8.duration = nbDuration.value;
	if(buttonValues.value == 1, {control.copy(obj8.control).value; obj8.control = control; buttonValues.value=0; updateObjects.value; updateGui.value}, {obj8.control = control});
	('## Set slot 6: ' ++ obj8.description).postln })};


	nbSpeed = NumberBox(window, Rect(xSlider, yNumberBoxUpper, buttonWidth, 20));
	nbSpeed.value = control.speed;
	nbDensity = NumberBox(window, Rect(xSlider + (xSliderOffset * 1), yNumberBoxUpper,buttonWidth, 20));
	nbDensity.value = control.density;
	nbFrequency = NumberBox(window, Rect(xSlider + (xSliderOffset * 2), yNumberBoxUpper,buttonWidth, 20));
	nbFrequency.value = control.frequency;
	nbEntropy = NumberBox(window, Rect(xSlider + (xSliderOffset * 3), yNumberBoxUpper,buttonWidth, 20));
	nbEntropy.value = control.entropy;
	nbAmplitude = NumberBox(window, Rect(xSlider + (xSliderOffset * 4), yNumberBoxUpper,buttonWidth, 20));
	nbAmplitude.value = control.amplitude;
	nbColor = NumberBox(window, Rect(xSlider + (xSliderOffset * 5), yNumberBoxUpper, buttonWidth, 20));
	nbColor.value = control.color;
	nbSurface = NumberBox(window, Rect(xSlider + (xSliderOffset * 6), yNumberBoxUpper, buttonWidth, 20));
	nbSurface.value = control.surface;
	nbLocation = NumberBox(window, Rect(xSlider + (xSliderOffset * 7), yNumberBoxUpper, buttonWidth, 20));
	nbLocation.value = control.location;
	nbPosition = NumberBox(window, Rect(xSlider + (xSliderOffset * 8), yNumberBoxUpper, buttonWidth, 20));
	nbPosition.value = control.location;

	nbAmp1 = NumberBox(window, Rect(xSlider, yNumberBoxLower, buttonWidth, 20));
	nbAmp1.value = 0.0;
	nbAmp1.scroll_step = 0.01;
	nbAmp1.clipLo = 0.0;
	nbAmp1.clipHi = 0.999;
	nbAmp2 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 1), yNumberBoxLower, buttonWidth, 20));
	nbAmp2.value = 0.0;
	nbAmp2.value = 0.0;
	nbAmp2.scroll_step = 0.01;
	nbAmp2.clipLo = 0.0;
	nbAmp2.clipHi = 0.999;
	nbAmp3 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 2), yNumberBoxLower, buttonWidth, 20));
	nbAmp3.value = 0.0;
	nbAmp3.value = 0.0;
	nbAmp3.scroll_step = 0.01;
	nbAmp3.clipLo = 0.0;
	nbAmp3.clipHi = 0.999;
	nbAmp4 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 3), yNumberBoxLower, buttonWidth, 20));
	nbAmp4.value = 0.0;
	nbAmp4.value = 0.0;
	nbAmp4.scroll_step = 0.01;
	nbAmp4.clipLo = 0.0;
	nbAmp4.clipHi = 0.999;
	nbAmp5 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 4), yNumberBoxLower, buttonWidth, 20));
	nbAmp5.value = 0.0;
	nbAmp5.value = 0.0;
	nbAmp5.scroll_step = 0.01;
	nbAmp5.clipLo = 0.0;
	nbAmp5.clipHi = 0.999;
	nbAmp6 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 5), yNumberBoxLower, buttonWidth, 20));
	nbAmp6.value = 0.0;
	nbAmp6.value = 0.0;
	nbAmp6.scroll_step = 0.01;
	nbAmp6.clipLo = 0.0;
	nbAmp6.clipHi = 0.999;
	nbAmp7 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 6), yNumberBoxLower, buttonWidth, 20));
	nbAmp7.value = 0.0;
	nbAmp7.value = 0.0;
	nbAmp7.scroll_step = 0.01;
	nbAmp7.clipLo = 0.0;
	nbAmp7.clipHi = 0.999;
	nbAmp8 = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 7), yNumberBoxLower, buttonWidth, 20));
	nbAmp8.value = 0.0;
	nbAmp8.value = 0.0;
	nbAmp8.scroll_step = 0.01;
	nbAmp8.clipLo = 0.0;
	nbAmp8.clipHi = 0.999;
	nbDuration = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 8), yNumberBoxLower, buttonWidth, 20));
	nbDuration.value = 100.0;
	nbRandFrom = NumberBox(window, Rect(xSlider, yRandom, buttonWidth, 20));
	nbRandFrom.value = 0.01;
	nbRandFrom.clipLo = 0.01;
	nbRandFrom.clipHi = 0.9999;
	nbRandFrom.scroll_step = 0.01;
	nbRandTo = NumberBox(window, Rect(xSlider + (xSliderOffsetLower * 1), yRandom, buttonWidth, 20));
	nbRandTo.value = 0.99;
	nbRandTo.clipLo = 0.0001;
	nbRandTo.clipHi = 0.9999;
	nbRandTo.scroll_step = 0.01;

	buttonRand1 = Button(window, Rect(xSlider, yButtonRand, buttonWidth, 20))
	.background_(sliderGUIColor)
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view| if(obj1.notNil && item1.notNil ,
	{})});

	buttonRand2 = Button(window, Rect(xSlider + (xSliderOffsetLower * 1), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view| if(obj2.notNil && item2.notNil ,
	{})});

	buttonRand3 = Button(window, Rect(xSlider + (xSliderOffsetLower * 2), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|  if(obj3.notNil && item3.notNil ,
	{})});

	buttonRand4 = Button(window, Rect(xSlider + (xSliderOffsetLower * 3), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|  if(obj4.notNil && item4.notNil ,
	{})});

	buttonRand5 = Button(window, Rect(xSlider + (xSliderOffsetLower * 4), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|  if(obj5.notNil && item5.notNil ,
	{})});

	buttonRand6 = Button(window, Rect(xSlider + (xSliderOffsetLower * 5), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|   if(obj6.notNil && item6.notNil ,
	{})});

	buttonRand7 = Button(window, Rect(xSlider + (xSliderOffsetLower * 6), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|   if(obj7.notNil && item7.notNil ,
	{})});

	buttonRand8 = Button(window, Rect(xSlider + (xSliderOffsetLower * 7), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|   if(obj8.notNil && item8.notNil ,
	{})});

	buttonRand9 = Button(window, Rect(xSlider + (xSliderOffsetLower * 8), yButtonRand, buttonWidth, 20))
	.states_([["Rand", buttonColorText, knobColor], ["- - -", buttonColorOnText, knobColorMore]])
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.action_({|view|   if(obj8.notNil && item8.notNil ,
	{})});

	buttonLearn1 = Button(window, Rect(xButton, yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader1.notNil, {midiFader1.remove})}, { midiFader1 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.speed = scaledValue; updateObjects.value; {sliderSpeed.value = scaledValue}.defer}); midiFader1.learn; midiFader1.postln})});

	buttonLearn2 = Button(window, Rect(xButton + (xButtonOffset * 1), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader2.notNil, {midiFader2.remove})}, { midiFader2 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.density = scaledValue; updateObjects.value; {sliderDensity.value = scaledValue}.defer}); midiFader2.learn; midiFader2.postln})});

	buttonLearn3 = Button(window, Rect(xButton + (xButtonOffset * 2), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader3.notNil, {midiFader3.remove})}, { midiFader3 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.frequency = scaledValue; updateObjects.value; {sliderFrequency.value = scaledValue}.defer}); midiFader3.learn; midiFader3.postln})});

	buttonLearn4 = Button(window, Rect(xButton + (xButtonOffset * 3), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader4.notNil, {midiFader4.remove})}, { midiFader4 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.entropy = scaledValue; updateObjects.value; {sliderEntropy.value = scaledValue}.defer}); midiFader4.learn; midiFader4.postln})});

	buttonLearn5 = Button(window, Rect(xButton + (xButtonOffset * 4), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader5.notNil, {midiFader5.remove})}, { midiFader5 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.amplitude = scaledValue; updateObjects.value; {sliderAmplitude.value = scaledValue}.defer}); midiFader5.learn; midiFader5.postln})});

	buttonLearn6 = Button(window, Rect(xButton + (xButtonOffset * 5), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader6.notNil, {midiFader6.remove})}, { midiFader6 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.color = scaledValue; updateObjects.value; {sliderColor.value = scaledValue}.defer}); midiFader6.learn; midiFader6.postln})});

	buttonLearn7 = Button(window, Rect(xButton + (xButtonOffset * 6), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader7.notNil, {midiFader7.remove})}, { midiFader7 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.surface = scaledValue; updateObjects.value; {sliderSurface.value = scaledValue}.defer}); midiFader7.learn; midiFader7.postln})});

	buttonLearn8 = Button(window, Rect(xButton + (xButtonOffset * 7), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader8.notNil, {midiFader8.remove})}, { midiFader8 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.location = scaledValue; updateObjects.value; {sliderLocation.value = scaledValue}.defer}); midiFader8.learn; midiFader8.postln})});

	buttonLearn9 = Button(window, Rect(xButton + (xButtonOffset * 8), yButtonLearnFader, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiFader9.notNil, {midiFader9.remove})}, { midiFader9 = CCResponder({ |src,chan,num,value| var scaledValue; scaledValue = value.linlin(0,127, 0.0001, 0.999); control.position = scaledValue; updateObjects.value; {sliderPosition.value = scaledValue}.defer}); midiFader9.learn; midiFader9.postln})});

	// Learn trigger

	buttonLearnTrigger1 = Button(window, Rect(xButton, ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({|view| if(view.value == 0, { if(midiButton1.notNil, {midiButton1.remove})},
	{
	     if(midiButton1.notNil, {midiButton1.remove});

		midiButton1 = CCResponder({ |src,chan,num,value|
		if(obj1.notNil && obj1IsPlaying == 0, {
		obj1.control.amplitude = value.linlin(0, 127, midiVelocityLower, midiVelocityUpper).postln;
		obj1.play; obj1IsPlaying = 1;
		{obj1.duration = nbDuration.value}.defer;
		{nbAmp1.value = obj1.control.amplitude}.defer;
		{buttonObj1.value = 1}.defer
		}, {

		if(obj1.notNil && obj1IsPlaying == 1, {
		obj1.stop; obj1IsPlaying = 0;
		{buttonObj1.value = 0}.defer; {nbAmp1.value = 0}.defer
		})})});
		midiButton1.learn;
	})});

	buttonLearnTrigger2 = Button(window, Rect(xButton + (xButtonOffset * 1), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
		.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
		.action_({|view| if(view.value == 0, { if(midiButton2.notNil, {midiButton2.remove})},
	{
	     if(midiButton2.notNil, {midiButton2.remove});

		midiButton2 = CCResponder({ |src,chan,num,value|
		if(obj2.notNil && obj2IsPlaying == 0, {
		obj2.control.amplitude = value.linlin(0, 122, midiVelocityLower, midiVelocityUpper).postln;
		obj2.play; obj2IsPlaying = 1;
		{obj2.duration = nbDuration.value}.defer;
		{nbAmp2.value = obj2.control.amplitude}.defer;
		{buttonObj2.value = 1}.defer
		}, {

		if(obj2.notNil && obj2IsPlaying == 1, {
		obj2.stop; obj2IsPlaying = 0;
		{buttonObj2.value = 0}.defer; {nbAmp2.value = 0}.defer
		})})});
		midiButton2.learn;
	})});

	buttonLearnTrigger3 = Button(window, Rect(xButton + (xButtonOffset * 2), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
		.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
		.action_({|view| if(view.value == 0, { if(midiButton3.notNil, {midiButton3.remove})},
	{
	     if(midiButton3.notNil, {midiButton3.remove});

		midiButton3 = CCResponder({ |src,chan,num,value|
		if(obj3.notNil && obj3IsPlaying == 0, {
		obj3.control.amplitude = value.linlin(0, 123, midiVelocityLower, midiVelocityUpper).postln;
		obj3.play; obj3IsPlaying = 1;
		{obj3.duration = nbDuration.value}.defer;
		{nbAmp3.value = obj3.control.amplitude}.defer;
		{buttonObj3.value = 1}.defer
		}, {

		if(obj3.notNil && obj3IsPlaying == 1, {
		obj3.stop; obj3IsPlaying = 0;
		{buttonObj3.value = 0}.defer; {nbAmp3.value = 0}.defer
		})})});
		midiButton3.learn;
	})});

	buttonLearnTrigger4 = Button(window, Rect(xButton + (xButtonOffset * 3), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
		.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
		.action_({|view| if(view.value == 0, { if(midiButton4.notNil, {midiButton4.remove})},
	{
	     if(midiButton4.notNil, {midiButton4.remove});

		midiButton4 = CCResponder({ |src,chan,num,value|
		if(obj4.notNil && obj4IsPlaying == 0, {
		obj4.control.amplitude = value.linlin(0, 124, midiVelocityLower, midiVelocityUpper).postln;
		obj4.play; obj4IsPlaying = 1;
		{obj4.duration = nbDuration.value}.defer;
		{nbAmp4.value = obj4.control.amplitude}.defer;
		{buttonObj4.value = 1}.defer
		}, {

		if(obj4.notNil && obj4IsPlaying == 1, {
		obj4.stop; obj4IsPlaying = 0;
		{buttonObj4.value = 0}.defer; {nbAmp4.value = 0}.defer
		})})});
		midiButton4.learn;
	})});

	buttonLearnTrigger5 = Button(window, Rect(xButton + (xButtonOffset * 4), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
		.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
		.action_({|view| if(view.value == 0, { if(midiButton5.notNil, {midiButton5.remove})},
	{
	     if(midiButton5.notNil, {midiButton5.remove});

		midiButton5 = CCResponder({ |src,chan,num,value|
		if(obj5.notNil && obj5IsPlaying == 0, {
		obj5.control.amplitude = value.linlin(0, 125, midiVelocityLower, midiVelocityUpper).postln;
		obj5.play; obj5IsPlaying = 1;
		{obj5.duration = nbDuration.value}.defer;
		{nbAmp5.value = obj5.control.amplitude}.defer;
		{buttonObj5.value = 1}.defer
		}, {

		if(obj5.notNil && obj5IsPlaying == 1, {
		obj5.stop; obj5IsPlaying = 0;
		{buttonObj5.value = 0}.defer; {nbAmp5.value = 0}.defer
		})})});
		midiButton5.learn;
	})});

	buttonLearnTrigger6 = Button(window, Rect(xButton + (xButtonOffset * 5), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.action_({|view| if(view.value == 0, { if(midiButton6.notNil, {midiButton6.remove})},
	{
	     if(midiButton6.notNil, {midiButton6.remove});

		midiButton6 = CCResponder({ |src,chan,num,value|
		if(obj6.notNil && obj6IsPlaying == 0, {
		obj6.control.amplitude = value.linlin(0, 126, midiVelocityLower, midiVelocityUpper).postln;
		obj6.play; obj6IsPlaying = 1;
		{obj6.duration = nbDuration.value}.defer;
		{nbAmp6.value = obj6.control.amplitude}.defer;
		{buttonObj6.value = 1}.defer
		}, {

		if(obj6.notNil && obj6IsPlaying == 1, {
		obj6.stop; obj6IsPlaying = 0;
		{buttonObj6.value = 0}.defer; {nbAmp6.value = 0}.defer
		})})});
		midiButton6.learn;
	})});

	buttonLearnTrigger7 = Button(window, Rect(xButton + (xButtonOffset * 6), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	.action_({|view| if(view.value == 0, { if(midiButton7.notNil, {midiButton7.remove})},
	{
	     if(midiButton7.notNil, {midiButton7.remove});

		midiButton7 = CCResponder({ |src,chan,num,value|
		if(obj7.notNil && obj7IsPlaying == 0, {
		obj7.control.amplitude = value.linlin(0, 127, midiVelocityLower, midiVelocityUpper).postln;
		obj7.play; obj7IsPlaying = 1;
		{obj7.duration = nbDuration.value}.defer;
		{nbAmp7.value = obj7.control.amplitude}.defer;
		{buttonObj7.value = 1}.defer
		}, {

		if(obj7.notNil && obj7IsPlaying == 1, {
		obj7.stop; obj7IsPlaying = 0;
		{buttonObj7.value = 0}.defer; {nbAmp7.value = 0}.defer
		})})});
		midiButton7.learn;
	})});

	buttonLearnTrigger8 = Button(window, Rect(xButton + (xButtonOffset * 7), ybuttonLearnTrigger, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.font_(defaultFont)
	.states_([["Learn", buttonColorText, knobColor], ["Rem", buttonColorText, knobColorMore]])
	    .action_({|view| if(view.value == 0, { if(midiButton8.notNil, {midiButton8.remove})},
	{
	     if(midiButton8.notNil, {midiButton8.remove});

		midiButton8 = CCResponder({ |src,chan,num,value|
		if(obj8.notNil && obj8IsPlaying == 0, {
		obj8.control.amplitude = value.linlin(0, 127, midiVelocityLower, midiVelocityUpper).postln;
		obj8.play; obj8IsPlaying = 1;
		{obj8.duration = nbDuration.value}.defer;
		{nbAmp8.value = obj8.control.amplitude}.defer;
		{buttonObj8.value = 1}.defer
		}, {

		if(obj8.notNil && obj8IsPlaying == 1, {
		obj8.stop; obj8IsPlaying = 0;
		{buttonObj8.value = 0}.defer; {nbAmp8.value = 0}.defer
		})})});
		midiButton8.learn;
	})});

	buttonLearnTrigger9 = Button(window, Rect(xButton + (xButtonOffset * 8), ybuttonLearnTrigger + 10, buttonWidth, 20))
	.background_(sliderGUIColor)
	.focusColor_(Color.red(alpha:0.7))
	.states_([["RemAll", buttonColorText, knobColorMore]])
	.font_(defaultFont)
	.action_({
	if(midiButton1.notNil, {midiButton1.remove});
	if(midiButton2.notNil, {midiButton2.remove});
	if(midiButton3.notNil, {midiButton3.remove});
	if(midiButton4.notNil, {midiButton4.remove});
	if(midiButton5.notNil, {midiButton5.remove});
	if(midiButton6.notNil, {midiButton6.remove});
	if(midiButton7.notNil, {midiButton7.remove});
	if(midiButton8.notNil, {midiButton8.remove});
	if(midiFader1.notNil, {midiFader1.remove});
	if(midiFader2.notNil, {midiFader2.remove});
	if(midiFader3.notNil, {midiFader3.remove});
	if(midiFader4.notNil, {midiFader4.remove});
	if(midiFader5.notNil, {midiFader5.remove});
	if(midiFader6.notNil, {midiFader6.remove});
	if(midiFader7.notNil, {midiFader7.remove});
	if(midiFader8.notNil, {midiFader8.remove});
	buttonLearnTrigger1.value = 0;
	buttonLearnTrigger2.value = 0;
	buttonLearnTrigger3.value = 0;
	buttonLearnTrigger4.value = 0;
	buttonLearnTrigger5.value = 0;
	buttonLearnTrigger6.value = 0;
	buttonLearnTrigger7.value = 0;
	buttonLearnTrigger8.value = 0;
	buttonLearn1.value = 0;
	buttonLearn2.value = 0;
	buttonLearn3.value = 0;
	buttonLearn4.value = 0;
	buttonLearn5.value = 0;
	buttonLearn6.value = 0;
	buttonLearn7.value = 0;
	buttonLearn8.value = 0;
	buttonLearn9.value = 0;
	});

	buttonSetDuration = Button(window, Rect(xButton + (xButtonOffset * 8), yPop + 5, buttonWidth, 20))
	.states_([["Set", buttonColorText, knobColor]])
	.font_(defaultFont)
	.action_({
	    if(obj1.notNil, {obj1.duration = nbDuration.value; ('duration: ' ++ obj1.duration).postln});
	    if(obj2.notNil, {obj2.duration = nbDuration.value});
	    if(obj3.notNil, {obj3.duration = nbDuration.value});
	    if(obj4.notNil, {obj4.duration = nbDuration.value});
	    if(obj5.notNil, {obj5.duration = nbDuration.value});
	    if(obj6.notNil, {obj6.duration = nbDuration.value});
	    if(obj7.notNil, {obj7.duration = nbDuration.value});
	    if(obj8.notNil, {obj8.duration = nbDuration.value});
	 });

	updateObjects = {

	if(obj1IsPlaying > 0,
			{ obj1.update });

		if(obj2IsPlaying > 0,
			{ obj2.update });

		if(obj3IsPlaying > 0,
			{ obj3.update });

		if(obj4IsPlaying > 0,
			{ obj4.update });

		if(obj5IsPlaying > 0,
			{ obj5.update  });

		if(obj6IsPlaying > 0,
			{ obj6.update });

		if(obj7IsPlaying > 0,
			{ obj7.update });

		 if(obj8IsPlaying > 0,
			{ obj8.update });
	};

	updateGui = {

	nbSpeed.value = control.speed;
	sliderSpeed.value = control.speed;
	nbDensity.value = control.density;
	sliderDensity.value = control.density;
	nbFrequency.value = control.frequency;
	sliderFrequency.value = control.frequency;
	nbEntropy.value = control.entropy;
	sliderEntropy.value = control.entropy;
	nbAmplitude.value = control.amplitude;
	sliderAmplitude.value = control.amplitude;
	nbColor.value = control.color;
	sliderColor.value = control.color;
	nbSurface.value = control.surface;
	sliderSurface.value = control.surface;
	nbLocation.value = control.location;
	sliderLocation.value = control.location;
	nbPosition.value = control.position;
	sliderPosition.value = control.position;
	};

	// SLIDERS, PARAMETERS

	sliderSpeed = Slider(window, Rect(xSlider,yFaderUpper,buttonWidth, 90))
	    	.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.speed = view.value; updateObjects.value; nbSpeed.value = view.value })
	    	.value = control.speed;
	sliderDensity = Slider(window, Rect(xSlider + (xSliderOffset * 1),yFaderUpper,buttonWidth, 90))
		.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.density = view.value; updateObjects.value; nbDensity.value = view.value })
	    	.value = control.density;
	sliderFrequency = Slider(window, Rect(xSlider + (xSliderOffset * 2),yFaderUpper,buttonWidth, 90))
	     .focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.frequency = view.value; updateObjects.value; nbFrequency.value = view.value })
	    	.value = control.frequency;
	sliderEntropy = Slider(window, Rect(xSlider + (xSliderOffset * 3),yFaderUpper,buttonWidth, 90))
		.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.entropy = view.value; updateObjects.value; nbEntropy.value = view.value })
	    	.value = control.entropy;
	sliderAmplitude = Slider(window, Rect(xSlider + (xSliderOffset * 4),yFaderUpper,buttonWidth, 90))
		.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.amplitude = view.value; updateObjects.value; nbAmplitude.value = view.value})
	    	.value = control.amplitude;
	sliderColor = Slider(window, Rect(xSlider + (xSliderOffset * 5),yFaderUpper,buttonWidth, 90))
		.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.color = view.value; updateObjects.value; nbColor.value = view.value})
	    	.value = control.color;
	sliderSurface = Slider(window, Rect(xSlider + (xSliderOffset * 6),yFaderUpper,buttonWidth, 90))
	    	.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.surface = view.value; updateObjects.value; nbSurface.value = view.value})
	    	.value = control.surface;
	sliderLocation = Slider(window, Rect(xSlider + (xSliderOffset * 7),yFaderUpper,buttonWidth, 90))
	    	.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.location = view.value; updateObjects.value; nbLocation.value = view.value})
	    	.value = control.location;
	sliderPosition = Slider(window, Rect(xSlider + (xSliderOffset * 8),yFaderUpper,buttonWidth, 90))
	    	.focusColor_(Color.red(alpha:0.7))
	    	.background_(sliderGUIColor)
	    	.knobColor_(knobColor)
	    	.action_({|view| control.position = view.value; updateObjects.value; nbPosition.value = view.value})
	    	.value = control.position;

	// TEXT

	StaticText(window,Rect(xText - 5, yTextUpper, 60, 20))
	.stringColor_(colorText)
	.string_("Speed")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 1) -2, yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Density")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 2) - 1, yTextUpper, 60, 20))
	.stringColor_(colorText)
	.string_("Frequency")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 3), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Entropy")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 4), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Amplitude")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 5), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Color")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 6), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Surface")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 7), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Location")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 8), yTextUpper, 55, 20))
	.stringColor_(colorText)
	.string_("Position")
	.align_(\center)
	.font_(defaultFont);

	StaticText(window,Rect(xText - 5, yTextLower, 60, 20))
	.stringColor_(colorText)
	.string_( "Amp1")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 1) -2, yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp2")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 2) - 6, yTextLower, 60, 20))
	.stringColor_(colorText)
	.string_("Amp3")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 3) - 1, yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp4")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 4)-2, yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp5")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 5), yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp6")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 6), yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp7")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 7), yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp8")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 7), yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Amp8")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 8), yTextLower, 55, 20))
	.stringColor_(colorText)
	.string_("Duration")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window,Rect(xText - 10, yRandomText, 80, 20))
	.stringColor_(colorText)
	.string_("Rand From")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 1) - 3, yRandomText, 55, 20))
	.stringColor_(colorText)
	.string_("Rand To")
	.align_(\center)
	.font_(defaultFont);
	StaticText(window, Rect(xText + (xTextOffset * 2) - 8, yRandomText, 145, 20))
	.stringColor_(colorText)
	.string_("Randomness methods :")
	.align_(\center)
	.font_(defaultFont);

	// BUTTONS

	buttonObj1 = Button(window, Rect(xButton, yButton, buttonWidth, 30))
		.font_(defaultFont)
	.background_(sliderGUIColor)
	.states_([["1 OFF", buttonColorText, buttonColor], ["1 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj1.notNil, {obj1.duration = nbDuration.value; obj1.duration.postln; obj1.play; obj1IsPlaying = 1;})}
						{if(obj1.notNil, {obj1.stop; obj1IsPlaying = 0;})}
			   });

	buttonObj2 = Button(window, Rect(xButton + (xButtonOffset * 1), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["2 OFF", buttonColorText, buttonColor], ["2 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj2.notNil, {obj2.duration = nbDuration.value; obj2.play; obj2IsPlaying = 1;})}
						{if(obj2.notNil, {obj2.stop; obj2IsPlaying = 0;})}
				});

	buttonObj3 = Button(window, Rect(xButton + (xButtonOffset * 2), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["3 OFF", buttonColorText, buttonColor], ["3 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj3.notNil, {obj3.duration = nbDuration.value; obj3.play; obj3IsPlaying = 1;})}
						{if(obj3.notNil, {obj3.stop; obj3IsPlaying = 0;})}
				  });

	buttonObj4 = Button(window, Rect(xButton + (xButtonOffset * 3), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["4 OFF", buttonColorText, buttonColor], ["4 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj4.notNil, {obj4.duration = nbDuration.value; obj4.play; obj4IsPlaying = 1;})}
						{if(obj4.notNil, {obj4.stop; obj4IsPlaying = 0;})}
				  });

	buttonObj5 = Button(window, Rect(xButton + (xButtonOffset * 4), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["5 OFF", buttonColorText, buttonColor], ["5 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj5.notNil, {obj5.duration = nbDuration.value; obj5.play; obj5IsPlaying = 1;})}
						{if(obj5.notNil, {obj5.stop; obj5IsPlaying = 0;})}
			   });

	buttonObj6 = Button(window, Rect(xButton + (xButtonOffset * 5), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["6 OFF", buttonColorText, buttonColor], ["6 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj6.notNil, {obj6.duration = nbDuration.value; obj6.play; obj6IsPlaying = 1;})}
						{if(obj6.notNil, {obj6.stop; obj6IsPlaying = 0;})}
			   });

	buttonObj7 = Button(window, Rect(xButton + (xButtonOffset * 6), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["7 OFF", buttonColorText, buttonColor], ["7 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj7.notNil, {obj7.duration = nbDuration.value; obj7.play; obj7IsPlaying = 1;})}
						{if(obj7.notNil, {obj7.stop; obj7IsPlaying = 0;})}
			   });

	buttonObj8 = Button(window, Rect(xButton + (xButtonOffset * 7), yButton, buttonWidth, 30))
		.font_(defaultFont)
	.states_([["8 OFF", buttonColorText, buttonColor], ["8 ON", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
						{if(obj8.notNil, {obj8.duration = nbDuration.value; obj8.play; obj8IsPlaying = 1;})}
						{if(obj8.notNil, {obj8.stop; obj8IsPlaying = 0;})}
			   });

	buttonObj9 = Button(window, Rect(xButton + (xButtonOffset * 8), yButton, buttonWidth, 20))
		.font_(defaultFont)
	.states_([["PRINT", buttonColorOnText, buttonColorOn], ["PRINT", buttonColorOnText, buttonColorOn]])
	.action_({|view|  if (view.value ==1)
	{
		printItems.value;
	}});

	buttonValues = Button(window, Rect(xButton + (xButtonOffset * 8), yButton + 25, buttonWidth, 20))
		.font_(defaultFont)
	.states_([["COPY", buttonColorText, buttonColor], ["LEAVE", buttonColorOnText, buttonColorOn]]);

	buttonRandom = Button(window, Rect(xButton + 140, yRandom, 50, 20))
		.font_(defaultFont)
	.states_([["rrand", buttonColorText, sliderGUIColor]])
	.action_({|view|

	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});

	    if(buttonRand1.value < 1, { control.speed = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand2.value < 1, { control.density = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand3.value < 1, { control.frequency = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand4.value < 1, {	control.entropy = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand5.value < 1, {	control.amplitude = rrand(nbRandFrom.value, nbRandTo.value)});	    if(buttonRand6.value < 1, {	control.color = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand7.value < 1, {	control.surface = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand8.value < 1, {	control.location = rrand(nbRandFrom.value, nbRandTo.value)});
	    if(buttonRand9.value < 1, {	control.position = rrand(nbRandFrom.value, nbRandTo.value)});
	    	updateObjects.value;
	    	updateGui.value;
	    	});

	buttonRandomExp = Button(window, Rect(xButton + 205, yRandom, 60, 20))
		.font_(defaultFont)
			.states_([["exprand", buttonColorText, sliderGUIColor]])
			.action_({|view|

	     if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});

	    	if(buttonRand1.value < 1, { control.speed = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand2.value < 1, { control.density = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand3.value < 1, { control.frequency = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand4.value < 1, { control.entropy = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand5.value < 1, { control.amplitude = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand6.value < 1, { control.color = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand7.value < 1, { control.surface = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand8.value < 1, { control.location = exprand(nbRandFrom.value, nbRandTo.value)});
	    	if(buttonRand9.value < 1, { control.position = exprand(nbRandFrom.value, nbRandTo.value)});
	    	updateObjects.value;
	    	updateGui.value;
	    	});

	buttonRandomBilin = Button(window, Rect(xButton + 277, yRandom, 60, 20))
		.font_(defaultFont)
			.states_([["bilinrand", buttonColorText, sliderGUIColor]])
			.action_({|view|

	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});

	    	if(buttonRand1.value < 1, { control.speed = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand2.value < 1, { control.density = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand3.value < 1, { control.frequency = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand4.value < 1, { control.entropy = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand5.value < 1, { control.amplitude = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand6.value < 1, { control.color = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand7.value < 1, { control.surface = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand8.value < 1, { control.location = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	if(buttonRand9.value < 1, { control.position = ((nbRandTo.value).abs).bilinrand.min(nbRandTo.value).max(nbRandFrom.value)});
	    	updateObjects.value;
	    	updateGui.value;
	    	});

	buttonRandomSum = Button(window, Rect(xButton + 350, yRandom, 60, 20))
		.font_(defaultFont)
	   	.states_([["brownian", buttonColorText, sliderGUIColor]])
			.action_({|view|
	    	 var brown, brownList;
	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});
	    	brown = Pbrown(nbRandFrom.value, nbRandTo.value, (nbRandFrom.value-nbRandTo.value)*0.25, inf);
	    	brownList = brown.asStream.nextN(9);

	    	if(buttonRand1.value < 1, { control.speed = brownList[0]});
	    	if(buttonRand2.value < 1, { control.density = brownList[1]});
	    	if(buttonRand3.value < 1, { control.frequency = brownList[2]});
	    	if(buttonRand4.value < 1, { control.entropy = brownList[4]});
	    	if(buttonRand5.value < 1, { control.amplitude = brownList[3]});
	    	if(buttonRand6.value < 1, { control.color = brownList[5]});
	    	if(buttonRand7.value < 1, { control.surface = brownList[6]});
	    	if(buttonRand8.value < 1, { control.location = brownList[7]});
	    	if(buttonRand9.value < 1, { control.position = brownList[8]});
	    	updateObjects.value;
	    	updateGui.value;

	   	 });

	buttonRandom1 = Button(window, Rect(xButton + 421, yRandom, 60, 20))
		.font_(defaultFont)
	   	.states_([["beta", buttonColorText, sliderGUIColor]])
			.action_({|view|
	    var beta, betaList;
	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});

	    	beta = Pbeta(nbRandFrom.value, nbRandTo.value, 0.1, 0.1, inf);
	    	betaList = beta.asStream.nextN(9);

	    	if(buttonRand1.value < 1, { control.speed = betaList[0]});
	    	if(buttonRand2.value < 1, { control.density = betaList[1]});
	    	if(buttonRand3.value < 1, { control.frequency = betaList[2]});
	    	if(buttonRand4.value < 1, { control.entropy = betaList[4]});
	    	if(buttonRand5.value < 1, { control.amplitude = betaList[3]});
	    	if(buttonRand6.value < 1, { control.color = betaList[5]});
	    	if(buttonRand7.value < 1, { control.surface = betaList[6]});
	    	if(buttonRand8.value < 1, { control.location = betaList[7]});
	    	if(buttonRand9.value < 1, { control.position = betaList[8]});
	    	updateObjects.value;
	    	updateGui.value;
	    	});

	buttonRandom2 = Button(window, Rect(xButton + 490, yRandom, 60, 20))
		.font_(defaultFont)
	   	.states_([["tend lo", buttonColorText, sliderGUIColor]])
			.action_({|view|
	     var hprand, iprandList;
	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});
	    	hprand = Plprand(nbRandFrom.value, nbRandTo.value, inf);
	    	iprandList = hprand.asStream.nextN(9);

	    	if(buttonRand1.value < 1, { control.speed = iprandList[0]});
	    	if(buttonRand2.value < 1, { control.density = iprandList[1]});
	    	if(buttonRand3.value < 1, { control.frequency = iprandList[2]});
	    	if(buttonRand4.value < 1, { control.entropy = iprandList[4]});
	    	if(buttonRand5.value < 1, { control.amplitude = iprandList[3]});
	    	if(buttonRand6.value < 1, { control.color = iprandList[5]});
	    	if(buttonRand7.value < 1, { control.surface = iprandList[6]});
	    	if(buttonRand8.value < 1, { control.location = iprandList[7]});
	    	if(buttonRand9.value < 1, { control.position = iprandList[8]});
	    	updateObjects.value;
	    	updateGui.value;

	    	});

	buttonRandom3 = Button(window, Rect(xButton + 560, yRandom, 60, 20))
		.font_(defaultFont)
	   	.states_([["tend hi", buttonColorText, sliderGUIColor]])
			.action_({|view|
	     var hprand, hprandList;
	    if(nbRandFrom.value > nbRandTo.value, {nbRandFrom.value = nbRandTo.value});
	    	hprand = Phprand(nbRandFrom.value, nbRandTo.value, inf);
	    	hprandList = hprand.asStream.nextN(9);

	    	if(buttonRand1.value < 1, { control.speed = hprandList[0]});
	    	if(buttonRand2.value < 1, { control.density = hprandList[1]});
	    	if(buttonRand3.value < 1, { control.frequency = hprandList[2]});
	    	if(buttonRand4.value < 1, { control.entropy = hprandList[4]});
	    	if(buttonRand5.value < 1, { control.amplitude = hprandList[3]});
	    	if(buttonRand6.value < 1, { control.color = hprandList[5]});
	    	if(buttonRand7.value < 1, { control.surface = hprandList[6]});
	    	if(buttonRand8.value < 1, { control.location = hprandList[7]});
	    	if(buttonRand9.value < 1, { control.position = hprandList[8]});
	    	updateObjects.value;
	    	updateGui.value;
	    	});
	}
}
