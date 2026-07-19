# ============================================================
#  日本語検索用 Elasticsearch イメージ
#  公式イメージに、日本語解析プラグインを追加する
# ============================================================
FROM docker.elastic.co/elasticsearch/elasticsearch:9.4.3

# 日本語の形態素解析プラグイン（kuromoji）
RUN bin/elasticsearch-plugin install --batch analysis-kuromoji

# 全角/半角などの正規化に使うプラグイン（icu_normalizer 用）
RUN bin/elasticsearch-plugin install --batch analysis-icu
