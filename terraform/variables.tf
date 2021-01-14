variable "ssh_client_cidr_list" {
  description = "踏み台サーバーへのSSH接続を許可するクライアント側のcidrリスト"
  type = list(string)
}

variable "jdk_version" {
  description = "使用するJDKのバージョン（設定例：8u275）"
  type = string
}

variable "maven_version" {
  description = "使用するMavenのバージョン（設定例：3.6.3）※メジャーバージョンは 3 前提"
  type = string
  default = "3.6.3"
}

variable "aws_access_key" {
  description = "アプリケーション起動時に環境変数に設定するAWSのアクセスキー"
  type = string
}

variable "aws_secret_access_key" {
  description = "アプリケーション起動時に環境変数に設定するAWSのシークレットアクセスキー"
  type = string
}

variable "git_branch_name" {
  description = "チェックアウトするブランチ名"
  type = string
}

variable "git_old_tag_name" {
  description = "修正前のバッチのバージョンに割り当てられたタグ"
  type = string
}

variable "git_new_tag_name" {
  description = "修正後のバッチのバージョンに割り当てられたタグ"
  type = string
}

variable "old_assembly_final_name" {
  description = "mvn package で生成されるzipファイルのベース名(修正前モジュール)"
  type = string
  default = "application-5-NEXT-SNAPSHOT-old"
}

variable "new_assembly_final_name" {
  description = "mvn package で生成されるzipファイルのベース名(修正後モジュール)"
  type = string
  default = "application-5-NEXT-SNAPSHOT-new"
}