import numpy as np
import sys
sys.path.insert(0, "../../../../../third_party/llama.cpp/gguf-py")

from llama_cpp import Llama
from gguf import GGUFReader

GGUF_PATH  = "./gguf/Nlu_Model-102M-F32-pooler.gguf"
# 全部从 GGUF 读取，不依赖任何外部文件
r = GGUFReader(GGUF_PATH)
fields = r.fields

# 标签列表（转换时已自动写入）
id2label = {i: s for i, s in enumerate(fields["bert.classifier.output_labels"].contents())}

# pooler 权重（KV 元数据）
pooler_w = np.array(fields["bert.classifier.pooler_weight"].contents(), dtype=np.float32).reshape(768, 768)
pooler_b = np.array(fields["bert.classifier.pooler_bias"].contents(),   dtype=np.float32)

# 分类头权重（张量）
weights = {t.name: np.array(t.data) for t in r.tensors}
cls_w = weights["cls.output.weight"]   # [9, 768]
cls_b = weights["cls.output.bias"]     # [9]

model = Llama(
    model_path=GGUF_PATH,
    embedding=True,
    pooling_type=2,
    n_ctx=512,
    verbose=False,
)

print("NLU 推理 (输入 quit 退出)")
while True:
    text = input("\n> ").strip()
    if not text:
        continue
    if text.lower() in ("quit", "exit", "q"):
        break

    cls_hidden = np.array(model.embed(text))
    pooler_out = np.tanh(pooler_w @ cls_hidden + pooler_b)
    logits     = cls_w @ pooler_out + cls_b

    exp   = np.exp(logits - logits.max())
    probs = exp / exp.sum()
    ranked = sorted(enumerate(probs), key=lambda x: -x[1])

    print(f"预测: {id2label[ranked[0][0]]}  ({ranked[0][1]:.1%})")
    for i, p in ranked[:3]:
        print(f"  {id2label[i]}: {p:.1%}")
