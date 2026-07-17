variable "region" {
  description = "AWS 리전 (모든 리소스 공통)"
  type        = string
  default     = "ap-northeast-2"
}

variable "state_bucket_prefix" {
  description = "state 버킷 이름 접두사. 실제 이름은 여기에 계정 ID가 붙어 전역 유일해진다."
  type        = string
  default     = "gilbut-tfstate"
}
