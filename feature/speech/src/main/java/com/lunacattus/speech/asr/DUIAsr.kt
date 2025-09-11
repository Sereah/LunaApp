package com.lunacattus.speech.asr

import com.aispeech.export.engines2.AILocalASREngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DUIAsr @Inject constructor() {

    private lateinit var engine: AILocalASREngine


}