#include "llama_jni.h"
#include "llama.h"
#include <string>
#include <android/log.h>

#define LOG_TAG "LLAMA_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
        Java_com_learningtutor_core_ml_LLMGenerator_nativeInit(
        JNIEnv *env,
        jobject thiz,
jstring model_path
) {
const char *model_path_cstr = env->GetStringUTFChars(model_path, nullptr);

LOGI("Loading model from: %s", model_path_cstr);

// Параметры модели
struct llama_model_params model_params = llama_model_default_params();
model_params.use_mmap = true;
model_params.use_mlock = false;

struct llama_model *model = llama_load_model_from_file(model_path_cstr, model_params);
if (!model) {
LOGE("Failed to load model: %s", model_path_cstr);
env->ReleaseStringUTFChars(model_path, model_path_cstr);
return 0;
}

// Параметры контекста
struct llama_context_params ctx_params = llama_context_default_params();
ctx_params.n_ctx = 2048; // Размер контекста
ctx_params.n_threads = 4; // Количество потоков
ctx_params.n_threads_batch = 4;

struct llama_context *ctx = llama_new_context_with_model(model, ctx_params);
if (!ctx) {
LOGE("Failed to create context");
llama_free_model(model);
env->ReleaseStringUTFChars(model_path, model_path_cstr);
return 0;
}

env->ReleaseStringUTFChars(model_path, model_path_cstr);

LOGI("Model loaded successfully");
return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT jstring JNICALL
        Java_com_learningtutor_core_ml_LLMGenerator_nativeGenerate(
        JNIEnv *env,
        jobject thiz,
jlong context_ptr,
        jstring prompt,
jint max_tokens
) {
llama_context *ctx = reinterpret_cast<llama_context *>(context_ptr);
if (!ctx) {
LOGE("Context is null");
return env->NewStringUTF("");
}

const char *prompt_cstr = env->GetStringUTFChars(prompt, nullptr);

LOGI("Generating with prompt: %s", prompt_cstr);

// Токенизируем промпт
std::vector<llama_token> tokens = llama_tokenize(ctx, prompt_cstr, true);

// Добавляем системный промпт для структурированного вывода
std::string system_prompt = "Ты — экспресс-репетитор по математике. Генерируй задания строго в JSON формате.";
std::vector<llama_token> sys_tokens = llama_tokenize(ctx, system_prompt.c_str(), true);
tokens.insert(tokens.begin(), sys_tokens.begin(), sys_tokens.end());

// Параметры генерации
llama_batch batch = llama_batch_init(tokens.size(), 0, 1);
for (size_t i = 0; i < tokens.size(); i++) {
llama_batch_add(batch, tokens[i], i, {0}, false);
}

// Генерация
std::string result;
int n_cur = batch.n_tokens;

while (n_cur < max_tokens) {
if (llama_decode(ctx, batch) != 0) {
LOGE("Failed to decode");
break;
}

// Выбираем следующий токен
int n_vocab = llama_n_vocab(llama_get_model(ctx));
float *logits = llama_get_logits_ith(ctx, batch.n_tokens - 1);

std::vector<llama_token_data> candidates;
candidates.reserve(n_vocab);
for (llama_token token_id = 0; token_id < n_vocab; token_id++) {
candidates.emplace_back(llama_token_data{token_id, logits[token_id], 0.0f});
}

llama_token_data_array candidates_p = {candidates.data(), candidates.size(), false};

// Температура и sampling
const float temp = 0.7f;
llama_sample_temp(ctx, &candidates_p, temp);

const llama_token new_token_id = llama_sample_token(ctx, &candidates_p);

// Проверяем на стоп-токены
if (new_token_id == llama_token_eos(llama_get_model(ctx))) {
break;
}

// Конвертируем токен в строку
std::string token_str = llama_token_to_piece(ctx, new_token_id);
result += token_str;

// Проверяем на завершение JSON
if (result.find("}") != std::string::npos &&
        std::count(result.begin(), result.end(), '{') ==
std::count(result.begin(), result.end(), '}')) {
break;
}

// Добавляем токен для следующей итерации
llama_batch_add(batch, new_token_id, n_cur, {0}, true);
n_cur++;

// Ограничиваем длину
if (result.length() > 1000) {
break;
}
}

llama_batch_free(batch);
env->ReleaseStringUTFChars(prompt, prompt_cstr);

LOGI("Generation complete, length: %zu", result.length());
return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_learningtutor_core_ml_LLMGenerator_nativeFree(
        JNIEnv *env,
jobject thiz,
        jlong context_ptr
) {
llama_context *ctx = reinterpret_cast<llama_context *>(context_ptr);
if (ctx) {
llama_free(ctx);
LOGI("Context freed");
}
}

} // extern "C"