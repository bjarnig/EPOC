SynthWrap {
	var <name, <source, <specs, <args, <argNames, <defaultSynthArgs, <runEditor, <vcas;
	var <group, <server, <synthDef, <synth;
	var <synthControlers;

	*new{ |asource, aspecs, initVals,rates,name,vcaBools|

		^super.new.init(asource,aspecs,initVals,rates,name,vcaBools)

	}

	init{ |asource, aspecs, initVals, rates, aname,vcaBools|

		if([Function,SynthDef,Symbol].includes(asource.class).not){
			Error("source has to be Function, Symbol or SynthDef").throw
		};

		source = asource;
		name = aname ? SystemSynthDefs.generateTempName;

		specs = IdentityDictionary.new;
		args = IdentityDictionary.new;
		defaultSynthArgs = IdentityDictionary.new;
		vcas = IdentityDictionary.new;

		switch(source.class)
			{Function} {
				synthDef = SynthDef(name, source,rates)
			 }
			{SynthDef} {
				synthDef = asource;
			}
			{Symbol}{
				synthDef = SynthDescLib.global[asource].def;

			};

		argNames = synthDef.allControlNames.reject{ |obj| obj.defaultValue.size > 0}.collect(_.name);
		vcaBools = vcaBools ?? { argNames.collect{ false } };

		argNames.do{ |key,i|
			var nedit, initVal;
			//("key is "++key++", index "++i++", spec is "++aspecs[i]).postln;
			initVal = if(initVals.notNil and: {initVals[i].notNil}){initVals[i]}{synthDef.controls[i]};
			defaultSynthArgs.put(key,initVal);

			if(aspecs.notNil){
				specs.put(key,aspecs[i]);
				nedit = if(aspecs[i].notNil){
					NumberEditor(initVal,aspecs[i])
				}{
					SimpleEditor(initVal)
				};
			}{
				specs.put(key,nil);
				nedit = SimpleEditor(initVal);
			};

			args.put(key,nedit);

			vcas.put(key,
				if(vcaBools[i]){
					//("putting vca for "++key).postln;
					LevelVCA(nedit)
				}
			);

		};

		runEditor = BooleanEditor(false);

	}

	*sdNew{ |name,initVals|
		^super.new.sdInit(name,initVals)
	}

	sdInit{ |aname,initVals|

		name = aname++rrand(1,5000);
		source = aname;

		specs = IdentityDictionary.new;
		args = IdentityDictionary.new;
		defaultSynthArgs = IdentityDictionary.new;
		vcas = IdentityDictionary.new;

		synthDef = SynthDescLib.global[source].def;

		//get names except controls for arrays
		argNames = synthDef.allControlNames.reject{ |obj| obj.defaultValue.size > 0 }.collect(_.name);

		argNames.do{ |key,i|
			var nedit, theInitVal, theSpec, theVcaBool;
			//("key is "++key++", index "++i++", spec is "++aspecs[i]).postln;
			theInitVal = initVals.tryPerform(\at,key);
			theInitVal = theInitVal ?? {synthDef.controls[i]};
			theSpec = SynthDescLib.global[source].metadata.tryPerform(\at,\specs).tryPerform(\at,key);
			theSpec = if(theSpec.isNil){nil}{theSpec.asSpec};
			theVcaBool = SynthDescLib.global[source].metadata.tryPerform(\at,\vcas).tryPerform(\at,key);

			//[key,theInitVal,theSpec,theVcaBool].postln;

			defaultSynthArgs.put(key,theInitVal);
			specs.put(key,theSpec);

			nedit = if(theSpec.notNil){
				NumberEditor(theInitVal,theSpec)
			}{
				SimpleEditor(theInitVal)
			};
			args.put(key,nedit);

			vcas.put(key,
				if(theVcaBool.notNil){
					//("putting vca for "++key).postln;
					LevelVCA(nedit)
				};
			);

		};

		runEditor = BooleanEditor(false);


	}

	currentValuesInArray{
		^this.currentValuesInArray2.flatten
	}

	currentValuesInArray2{
		^argNames.collect{ |name| [name,args[name].value] }
	}

	currentValuesInArray3{
		^argNames.collect{ |name| args[name].value }
	}

	currentValuesInArrayNoBusses{
		var list = List.new;
		argNames.do{ |name|
			if(name.asString.contains("bus").not) {
				 list.add([name,args[name].value])
			}
		};
		^list.as(Array)

	}

	//Getting and Setting values

	get{ |key|
		^args[key].value
	}

	set{ |key,value,change=true|
		//("setting "++key++" to "++value).postln;
		args[key].value_(value,change);
	}

	setN{ |array|
		if(array.size != argNames.size){
			Error("wrong size of array").throw
		};

		argNames.do{ |name,i| args[name].value(array[i]) }

	}

	setn{ |argname, array|
		var args, index;
		index = argNames.indexOf(argname);
		args = argNames[index..(index+array.size-1)];
		args.do{ |item,i|
			this.set(item,array[i]);
			//("setting "++argNames[index+i]++"to "++array[i]).postln;
		}
	}


	getByIndex{ |index|
		^this.get(argNames[index])
	}

	setByIndex{ |index,value|
		this.set(argNames[index],value)
	}

	addToValueIndex{ |index,value|
		this.setByIndex(index,value+this.getByIndex(index))
	}

	addToValueUnMappedIndex{ |index,value|
		this.setUnmapByIndex(index,value+this.getUnmapByIndex(index))
	}

	addToValueUnmapped{ |key,value|
		this.setUnmapped(key,this.getUnmapped(key)+value)
	}

	argNamesNoBus{
		^argNames.copy.takeThese({ |name| name.asString.contains("bus") })
	}

	addToValueUnMappedIndexNoBusses{ |index,value|
		var argNames = this.argNamesNoBus;
		this.setUnmapped(argNames[index],value+this.getUnmapped(argNames[index]))
	}

	getUnmapped{ |key|
		^args[key].unmappedValue
	}

	setUnmapped{ |key,value,change=true|
		args[key].setUnmappedValue(value,change);
	}

	getUnmapByIndex{ |index|
		^this.getUnmapped(argNames[index])
	}

	setUnmapByIndex{ |index,value|
		this.setUnmapped(argNames[index],value)
	}

	//playing, stoping, bundling, freeing

	prepareToPlay{ |atarget|

		atarget = (atarget ? ClusterServer.default).clusterfy.asTarget;
		group = atarget;
		server = atarget.server;

		if(source.class != Symbol){
			synthDef.sendCluster(server);
		};
		this.run(true,true);

	}

	playToBundle{ |bundle,paused=false|
		var numSeconds = 7, counter = 0;
		bundle = bundle.asClusterBundle;

		synth = ClusterSynth.basicNew(synthDef.name,server);
		synth.registerNodeWatcher;

		bundle.add(synth.addToTailMsg(group,this.currentValuesInArray));
		if(paused){
			this.addRunMsg(bundle,false)
		};
		fork{
		var func = if(synth.items.size == 1){
			{synth.isRunning.items[0].not}
		}{
			{synth.isRunning.not.items.reduce('||')}
		};
		/*block{ |break|
		while(func){
			//"waiting for synth to register".postln;
			0.1.wait;
			counter = counter +1;
			if(counter == (numSeconds*10)){
				"[SynthWrap] Synth did not start running. controls not initialized".postln;
				break.value;
			};
		};

		//"registering controls".postln;
		*/
			this.registerSynthControlers;
			runEditor.value_(true,false);
		//};
		}


	}

	play{ |target, addAction = \addToTail |
		var numSeconds = 7, counter = 0;

		if(target.isNil){ target = ClusterServer([Server.default]) };

		synth = synthDef.playCluster(target,this.currentValuesInArray,addAction);
		synth.registerNodeWatcher;
		fork{
		var func = if(synth.items.size == 1){
			{synth.isRunning.items[0].not}
		}{
			{synth.isRunning.not.items.reduce('||')}
		};
		block{ |break|
		while(func){
			//"waiting for synth to register".postln;
			0.1.wait;
			counter = counter +1;
			if(counter == (numSeconds*10)){
				"[SynthWrap] Synth did not start running. controls not initialized".postln;
				break.value;
			};
		};

		//"registering controls".postln;
			this.registerSynthControlers;
			runEditor.value_(true,false);
		}
		};

		^synth

	}



	run{ |bool,change=true|
		runEditor.value_(bool);
		if(change,{ runEditor.changed });
	}

	addSetMsg{ arg bundle ... args;
		bundle.add(synth.setMsg(*args))
	}

	addRunMsg{ |bundle,bool|
		bundle.add(synth.runMsg(bool))
	}

	isPlaying
	{
		//yealds true if any of the servers is playing
		var bools;
		^if(synth.isNil)
			{false}
			{
				bools = synth.items.collect(_.isPlaying ? false);
				if(bools.size ==1)
					{bools[0]}
					{bools.reduce('or') }
			}


    }

	runStatus{
		^runEditor.value
	}

	//CONTROLLERS MVC
	registerSynthControlers{
		//("running registerSynthControlers for "++this.hash).postln;
		synthControlers = args.collect{Ê|numEdit,key|
			var accessNumEdit = if(vcas[key].isNil){
				numEdit
			}{
				vcas[key].resultNEd
			};
			ActController(accessNumEdit,{ |obj|

			synth.set(key,obj.value);
			//("ActController(accessNumEdit), Setting synth's "++key++" to "++obj.value).postln;
 		})

		}.asArray;

		synthControlers = synthControlers.add(
			ActController(runEditor,{ |bool| synth.run(bool.value) })
			)

	}

	unregisterSynthControlers{
		synthControlers.do(_.remove)
	}
	//register to get updates of synth controlers

	register{ |key,function|
		^ActController(args[key],function)
	}

	registerRun{ |function|
		^ActController(runEditor,function)

	}

	free{
		if(synth.notNil and: {this.isPlaying}){synth.free;};
		this.freeControlers;
	}

	freeControlers{
		this.unregisterSynthControlers;
		//if(synth.notNil){synth.unregisterNodeWatcher};
		runEditor.value_(false);

	}

	gui{
		^SimpleHidableSynthWrapGui(this)
	}

}





