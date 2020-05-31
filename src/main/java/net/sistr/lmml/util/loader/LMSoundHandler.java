package net.sistr.lmml.util.loader;

import net.sistr.lmml.util.loader.resource.JsonResourceLittleMaidSound;
import net.sistr.lmml.util.loader.resource.ResourceFileHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * メイドさんのサウンド関連ファイルをロードする
 * 
 * @author firis-games
 *
 */
public class LMSoundHandler implements ILMFileLoaderHandler {

	public static LMSoundHandler instance = new LMSoundHandler();
	
	/**
	 * oggファイル一時保管用
	 */
	protected Map<String, List<String>> workOggFileList = new HashMap<>();
	
	/**
	 * Configファイル一時保管用
	 */
	protected Map<String, Map<String, String>> workConfigList = new HashMap<>();
	
	/**
	 * サウンドパック一覧
	 */
	public static JsonResourceLittleMaidSound resourceLittleMaidSound = new JsonResourceLittleMaidSound();
	
	/**
	 * キャッシュフラグ
	 */
	private boolean isCache = false;
	
	/**
	 * キャッシュファイル名
	 */
	private final String cacheFileName = "cache_soundpack.json";

	/**
	 * サウンドHandlerの初期化処理
	 * キャッシュ確認しキャッシュがあれば読込する
	 */
	@Override
	public void init() {
		
		//キャッシュ機能の利用可否
		if (true/*!LMRConfig.cfg_loader_is_cache*/) return;
		
		//キャッシュファイルの読み込み
		resourceLittleMaidSound = ResourceFileHelper.readFromJson(this.cacheFileName, JsonResourceLittleMaidSound.class);
		
		if (resourceLittleMaidSound != null) {
			this.isCache = true;
		} else {
			//初期化
			resourceLittleMaidSound = new JsonResourceLittleMaidSound();
		}
	}
	
	/**
	 * キャッシュがある場合は読み込み処理を行わない
	 */
	@Override
	public boolean isFileLoad() {
		return !this.isCache;
	}
	
	/**
	 * 対象ファイルがサウンド関連のファイルか判断する
	 * 
	 * ・拡張子が.ogg or ファイル名がlittleMaidMob.cfg
	 *　・サウンドファイルはzip or jar形式のもののみ対象とする
	 */
	@Override
	public boolean isLoadHandler(String path, Path filePath) {
		
		//圧縮ファイル以外は除外
		if (filePath == null) return false;
		
		//.拡張子判定
		return path != null && (path.endsWith(".ogg") || path.endsWith("littleMaidMob.cfg"));
	}
	
	
	/**
	 * サウンド関連ファイルをロード
	 * 
	 *　現時点では.oggは不要なファイルが含まれるため
	 *　すべて読込が終わったあとに最終的なデータを生成する
	 */
	@Override		
	public void loadHandler(String path, Path filePath, InputStream inputstream) {
		
		String fileName = filePath.getFileName().toString();
		
		//oggファイル処理
		if (path.endsWith(".ogg")) {
			//oggファイルを処理する
			this.loadHandlerOgg(path, fileName);
		//configファイル処理
		} else if (path.endsWith("littleMaidMob.cfg")) {
			//Configファイルを処理する
			this.loadHandlerConfig(path, fileName, inputstream);
		}
	}
	
	/**
	 * oggファイルを処理する
	 * 
	 * oggファイルは一度リストへ保管する
	 */
	protected void loadHandlerOgg(String path, String fileName) {
		
		//キーがない場合作成する
		if (!workOggFileList.containsKey(fileName)) {
			workOggFileList.put(fileName, new ArrayList<>());
		}
		
		//パスを追加する
		workOggFileList.get(fileName).add(path);
		
	}
	
	/**
	 * Configファイルを処理する
	 * 
	 */
	protected void loadHandlerConfig(String path, String fileName, InputStream inputstream) {
		
		//ClassLoaderから中身を読み込む
		List<String> texts = this.getTextFromInputStream(inputstream);
		
		Map<String, String> textMap = new HashMap<>();
		//テキストファイル処理
		for (String text : texts) {
			//先頭が#の場合は無視
			if (text.startsWith("#")) continue;
			
			//=1つ以外の場合は無視
			String[] params = text.split("=");
			if (params.length != 2) continue;
			
			//設定値を保存する
			textMap.put(params[0], params[1]);
		}
		
		//textMapにデータが存在する場合は保存
		//Configファイルは1ファイルにつき1つしかない想定
		if (textMap.size() > 0) {
			workConfigList.put(fileName, textMap);
		}
	}
	
	/**
	 * ClassLoader内のテキストファイルを読み込む
	 * @param path
	 * @return
	 */
	private List<String> getTextFromInputStream(InputStream path) {
		List<String> textList = new ArrayList<>();
		//テキストファイルを読み込む
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				textList.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textList;
	}
	
	/**
	 * サウンド関連ファイルの読込後処理
	 * 
	 * oggとconfig情報をもとに
	 * 
	 */
	@Override
	public void postLoadHandler() {
		
		//ファイル単位のループ
		for (String filekey : this.workConfigList.keySet()) {
			
			Map<String, String> cfgMap = this.workConfigList.get(filekey);
			List<String> oggList = this.workOggFileList.get(filekey);

			String voicePackageName = "";
			Map<String, List<String>> voicePackageList = new HashMap<>();
			float voiceRate = 1.0F;

			//Config単位でループ
			for (String voiceKey : cfgMap.keySet()) {
				
				String voiceValue = cfgMap.get(voiceKey);
				
				//キーがse_から始まる場合はボイス設定
				if (voiceKey.startsWith("se_")) {
					
					//littleMaidMob.を削除しパス形式をリソースパスのアクセス形式に変換する
					String searhPath = voiceValue.replace("littleMaidMob.", "").replace(".", "/");
					
					//oggListの中でsearchPathを含むものを探す
					List<String> voiceList = oggList.stream()
							.filter(v -> v.contains(searhPath))
							.collect(Collectors.toList());
					
					//パッケージ名がない場合はsearchPathから生成する
					if (voicePackageName.equals("")) {
						//一番後ろの各効果音の名前を排除する
						voicePackageName = searhPath.substring(0, searhPath.lastIndexOf("/"));
					}
					
					String voideId = voiceKey.replace("se_", "");
					
					//VoiceListを保存する
					if (voiceList.size() > 0) {
						voicePackageList.put(voideId, voiceList);
					}
					
				//VoiceRateを取得する
				} else if (voiceKey.toLowerCase().equals("LivingVoiceRate".toLowerCase())) {
					try {
						voiceRate = Float.parseFloat(voiceValue);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			//パッケージがない場合
			if (voicePackageName.equals("")) {
				voicePackageName = filekey;
			}
			
			//データが生成できた場合は保存する
			if (voicePackageList.size() > 0) {
				//サウンドパックを追加する
				resourceLittleMaidSound.addSoundpack(voicePackageName, voiceRate, voicePackageList);
			}
		}
		
		//キャッシュファイルを出力する
		if (false/*LMRConfig.cfg_loader_is_cache*/) {
			ResourceFileHelper.writeToJson(this.cacheFileName, resourceLittleMaidSound);
		}	
	}
}
