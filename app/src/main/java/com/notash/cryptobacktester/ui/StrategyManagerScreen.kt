package com.notash.cryptobacktester.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notash.cryptobacktester.robot.AlvexRobotImporter
import com.notash.cryptobacktester.strategy.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipInputStream

private const val PREFS="alvex_strategy_manager"
private const val HISTORY="history"
private const val ACTIVE="active"

@Composable
fun StrategyManagerScreen(fa:Boolean,onActiveStrategy:(ManagedStrategy)->Unit){
 val context=LocalContext.current; val prefs=remember{context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)}
 var manager by remember{mutableStateOf(loadState(prefs))}; var editing by remember{mutableStateOf<ManagedStrategy?>(null)}; var deleting by remember{mutableStateOf<ManagedStrategy?>(null)}; var uri by remember{mutableStateOf<Uri?>(null)}; var notice by remember{mutableStateOf<String?>(null)}; var error by remember{mutableStateOf<String?>(null)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri=it}
 fun apply(next:StrategyManagerState){manager=next;persist(prefs,next);next.history.firstOrNull{it.key==next.activeIdVersion}?.let{registerRobot(it);onActiveStrategy(it)}}
 LaunchedEffect(Unit){manager.history.forEach(::registerRobot);manager.history.firstOrNull{it.key==manager.activeIdVersion}?.let(onActiveStrategy)}
 LaunchedEffect(uri){val selected=uri?:return@LaunchedEffect;runCatching{val(json,source)=readFile(context,selected);val r=AlvexRobotImporter.fromJson(json);val s=ManagedStrategy(r.id,r.name,r.version,StrategyType.ROBOT,"LONG rule enabled=${r.rules.longWhenCloseAboveOpen}","SL ${r.parameters.stopLossPercent}% / TP ${r.parameters.takeProfitPercent}%","Risk ${r.parameters.riskPercent}%","15m",100.0,r.parameters.leverage,System.currentTimeMillis(),source,json);registerRobot(s);apply(StrategyManagerCore(manager.history,manager.activeIdVersion).saveAsNewVersion(s));notice=if(fa)"استراتژی وارد و فعال شد"else"Strategy imported and activated";error=null}.onFailure{error=it.message?:"Invalid strategy file";notice=null};uri=null}
 if(editing!=null){StrategyEditor(fa,editing!!,{editing=null}){updated->runCatching{apply(StrategyManagerCore(manager.history,manager.activeIdVersion).saveAsNewVersion(updated));editing=null;notice=if(fa)"نسخه جدید ذخیره و فعال شد"else"New version saved and activated"}.onFailure{error=it.message}};return}
 deleting?.let{target->AlertDialog(onDismissRequest={deleting=null},title={Text(if(fa)"حذف نسخه؟"else"Delete version?")},text={Text("${target.name} • ${target.version}")},confirmButton={TextButton(onClick={apply(StrategyManagerCore(manager.history,manager.activeIdVersion).delete(target.id,target.version,true));deleting=null}){Text(if(fa)"حذف"else"Delete")}},dismissButton={TextButton(onClick={deleting=null}){Text(if(fa)"لغو"else"Cancel")}})}
 LazyColumn(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
  item{Card(colors=CardDefaults.cardColors(containerColor=Panel),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Strategy Manager",color=PrimaryText,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);Text(if(fa)"مدیریت واقعی، نسخه‌بندی و سابقه استراتژی‌ها"else"Real strategy management, versioning and history",color=Muted);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({editing=newStrategy()},Modifier.weight(1f)){Icon(Icons.Outlined.Add,null);Spacer(Modifier.width(4.dp));Text(if(fa)"جدید"else"New")};OutlinedButton({picker.launch(arrayOf("application/json","application/zip","text/plain"))},Modifier.weight(1f)){Icon(Icons.Outlined.FileOpen,null);Spacer(Modifier.width(4.dp));Text("Import JSON/ZIP")}}}}}
 notice?.let{item{Text(it,color=Mint,fontWeight=FontWeight.Bold)}};error?.let{item{Text(it,color=Red,fontWeight=FontWeight.Bold)}}
 if(manager.history.isEmpty())item{Text(if(fa)"سابقه‌ای وجود ندارد"else"No strategy history",color=Muted)}
 items(manager.history.sortedByDescending{it.createdAt}){s->val active=s.key==manager.activeIdVersion;Card(colors=CardDefaults.cardColors(containerColor=if(active)Panel2 else Panel),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(s.name,color=PrimaryText,fontWeight=FontWeight.Bold);Text("${s.type.name} • v${s.version} • ${s.timeframe}",color=Muted)};if(active)AssistChip({},label={Text(if(fa)"فعال"else"ACTIVE")})};Text("Entry: ${s.entryRules}",color=PrimaryText,style=MaterialTheme.typography.bodySmall);Text("Exit: ${s.exitRules}",color=Muted,style=MaterialTheme.typography.bodySmall);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton({editing=s},Modifier.weight(1f)){Icon(Icons.Outlined.Edit,null);Spacer(Modifier.width(3.dp));Text(if(fa)"ویرایش / نسخه"else"Edit / Version")};OutlinedButton({apply(StrategyManagerCore(manager.history,manager.activeIdVersion).activate(s.id,s.version));notice=if(fa)"نسخه فعال شد"else"Activated"},enabled=!active,Modifier.weight(1f)){Icon(Icons.Outlined.Check,null);Spacer(Modifier.width(3.dp));Text(if(fa)"فعال"else"Activate")};IconButton({deleting=s}){Icon(Icons.Outlined.Delete,null,tint=Red)}}}}}
 }
}

@Composable private fun StrategyEditor(fa:Boolean,initial:ManagedStrategy,onCancel:()->Unit,onSave:(ManagedStrategy)->Unit){var name by remember(initial.key){mutableStateOf(initial.name)};var version by remember(initial.key){mutableStateOf(initial.version)};var entry by remember(initial.key){mutableStateOf(initial.entryRules)};var exit by remember(initial.key){mutableStateOf(initial.exitRules)};var risk by remember(initial.key){mutableStateOf(initial.riskRules)};var timeframe by remember(initial.key){mutableStateOf(initial.timeframe)};var amount by remember(initial.key){mutableStateOf(initial.tradeAmount.toString())};var leverage by remember(initial.key){mutableStateOf(initial.leverage.toString())};LazyColumn(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text(if(fa)"ویرایش / ساخت نسخه جدید"else"Edit / New Version",color=PrimaryText,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black)};item{OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text(if(fa)"نام"else"Name")},singleLine=true)};item{OutlinedTextField(version,{version=it},Modifier.fillMaxWidth(),label={Text("Version")},singleLine=true)};item{OutlinedTextField(entry,{entry=it},Modifier.fillMaxWidth(),label={Text(if(fa)"قوانین ورود"else"Entry Rules")},minLines=2)};item{OutlinedTextField(exit,{exit=it},Modifier.fillMaxWidth(),label={Text(if(fa)"خروج / SL / TP"else"Exit / SL / TP")},minLines=2)};item{OutlinedTextField(risk,{risk=it},Modifier.fillMaxWidth(),label={Text(if(fa)"ریسک"else"Risk Rules")},minLines=2)};item{OutlinedTextField(timeframe,{timeframe=it},Modifier.fillMaxWidth(),label={Text("Timeframe")},singleLine=true)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(amount,{amount=it},Modifier.weight(1f),label={Text(if(fa)"مبلغ ورود"else"Trade Amount")},singleLine=true);OutlinedTextField(leverage,{leverage=it},Modifier.weight(1f),label={Text("Leverage")},singleLine=true)}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onCancel,Modifier.weight(1f)){Text(if(fa)"لغو"else"Cancel")};Button({onSave(initial.copy(name=name.trim(),version=version.trim(),entryRules=entry.trim(),exitRules=exit.trim(),riskRules=risk.trim(),timeframe=timeframe.trim(),tradeAmount=amount.toDoubleOrNull()?:0.0,leverage=leverage.toDoubleOrNull()?:0.0,createdAt=System.currentTimeMillis()))},Modifier.weight(1f)){Text(if(fa)"ذخیره نسخه جدید"else"Save New Version")}}}}
}

private fun newStrategy()=ManagedStrategy("manual_${System.currentTimeMillis()}","New Strategy","1.0.0",StrategyType.MANUAL,"Define entry condition","Define exit / SL / TP","Risk per trade","15m",100.0,3.0,System.currentTimeMillis(),StrategySource.MANUAL)
private fun registerRobot(s:ManagedStrategy){s.robotJson?.let{runCatching{StrategyFactory.registerImportedRobot(AlvexRobotImporter.fromJson(it))}}}
private fun loadState(p:android.content.SharedPreferences):StrategyManagerState{val raw=p.getString(HISTORY,null);val list=if(raw==null)listOf(ManagedStrategy("advanced_pullback_v1","Advanced Pullback","1.0.0",StrategyType.ROBOT,"LWMA20 > LWMA50 + ATR","SL 1.5 ATR / TP 3 ATR","1% risk","15m",100.0,3.0,1L,StrategySource.MANUAL))else runCatching{val a=JSONArray(raw);(0 until a.length()).map{fromJson(a.getJSONObject(it))}}.getOrDefault(emptyList());return StrategyManagerState(list,p.getString(ACTIVE,list.firstOrNull()?.key))}
private fun persist(p:android.content.SharedPreferences,state:StrategyManagerState){val a=JSONArray();state.history.forEach{a.put(toJson(it))};p.edit().putString(HISTORY,a.toString()).putString(ACTIVE,state.activeIdVersion).apply()}
private fun toJson(s:ManagedStrategy)=JSONObject().apply{put("id",s.id);put("name",s.name);put("version",s.version);put("type",s.type.name);put("entry",s.entryRules);put("exit",s.exitRules);put("risk",s.riskRules);put("timeframe",s.timeframe);put("amount",s.tradeAmount);put("leverage",s.leverage);put("created",s.createdAt);put("source",s.source.name);put("robotJson",s.robotJson)}
private fun fromJson(o:JSONObject)=ManagedStrategy(o.getString("id"),o.getString("name"),o.getString("version"),StrategyType.valueOf(o.optString("type","ROBOT")),o.optString("entry"),o.optString("exit"),o.optString("risk"),o.optString("timeframe","15m"),o.optDouble("amount",100.0),o.optDouble("leverage",3.0),o.optLong("created",System.currentTimeMillis()),StrategySource.valueOf(o.optString("source","JSON")),o.optString("robotJson",null))
private fun readFile(context:Context,uri:Uri):Pair<String,StrategySource>{val type=context.contentResolver.getType(uri).orEmpty();if(type.contains("zip")||uri.toString().endsWith(".zip",true)){val input=context.contentResolver.openInputStream(uri)?:error("Unable to open ZIP");ZipInputStream(input).use{z->var e=z.nextEntry;while(e!=null){if(!e.isDirectory&&e.name.endsWith(".json",true))return z.readBytes().toString(Charsets.UTF_8) to StrategySource.ZIP;e=z.nextEntry};error("ZIP does not contain JSON")}};return(context.contentResolver.openInputStream(uri)?:error("Unable to open file")).use{it.readBytes().toString(Charsets.UTF_8)} to StrategySource.JSON}
