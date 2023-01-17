terraform {
  required_version = "~> 1.3.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.49.0"
    }
  }

  backend "s3" {
    bucket = "appi-infrastructure-terraform-state"
    key    = "distroy.tfstate"
    region = "us-east-1"
  }
}
provider "aws" {
  alias  = "acm_provider"
  region = "us-east-1"
}

locals {
  config = {
    defaults = {
      site = "distroy"
      domain_name = "distroy.com"
    }

    beta = {
      domain_prefix = "beta."
    }
    production = {
      domain_prefix = ""
    }
  }

  environment = terraform.workspace
  deployment = merge(local.config.defaults, local.config[terraform.workspace])
  domain_name = "${local.deployment.domain_prefix}${local.deployment.domain_name}"
  bucket_name = "${local.deployment.domain_name}-${local.environment}"

  tags = {
    Namespace = local.domain_name
    Environment = local.environment
    Owner = "Terrence Curran"
    ManagedBy = "Terraform"
  }
}

# Route 53 for domain
resource "aws_route53_zone" "main" {
  name = local.domain_name
  tags = local.tags
}

resource "aws_route53_record" "root-a" {
  zone_id = aws_route53_zone.main.zone_id
  name    = local.domain_name
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.root_s3_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.root_s3_distribution.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "www-a" {
  zone_id = aws_route53_zone.main.zone_id
  name    = "www.${local.domain_name}"
  type    = "A"
  alias {
    name                   = aws_cloudfront_distribution.root_s3_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.root_s3_distribution.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.ssl_certificate.domain_validation_options : dvo.domain_name => {
      name    = dvo.resource_record_name
      record  = dvo.resource_record_value
      type    = dvo.resource_record_type
      zone_id = aws_route53_zone.main.zone_id
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = each.value.zone_id
}

resource "aws_acm_certificate" "ssl_certificate" {
  provider                  = aws.acm_provider
  domain_name               = local.domain_name
  subject_alternative_names = ["*.${local.domain_name}"]
  validation_method         = "DNS"

  tags = local.tags

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_acm_certificate_validation" "cert_validation" {
  provider        = aws.acm_provider
  certificate_arn = aws_acm_certificate.ssl_certificate.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}


resource "aws_cloudfront_distribution" "www_s3_distribution" {
  origin {
    domain_name = aws_s3_bucket_website_configuration.www_bucket.website_endpoint
    origin_id   = "s3-www.${local.bucket_name}"
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1", "TLSv1.1", "TLSv1.2"]
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  aliases = ["www.${local.domain_name}"]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "s3-www.${local.bucket_name}"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 31536000
    default_ttl            = 31536000
    max_ttl                = 31536000
    compress               = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.cert_validation.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.1_2016"
  }

  tags = local.tags
}

resource "aws_cloudfront_distribution" "root_s3_distribution" {
  origin {
    domain_name = aws_s3_bucket_website_configuration.root_bucket.website_endpoint
    origin_id   = "s3-${local.bucket_name}"
    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols   = ["TLSv1", "TLSv1.1", "TLSv1.2"]
    }
  }

  enabled         = true
  is_ipv6_enabled = true

  aliases = [local.domain_name, "cloudfront.${local.domain_name}"]

  custom_error_response {
    error_caching_min_ttl = 0
    error_code            = 404
    response_code         = 200
    response_page_path    = "/404.html"
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "s3-${local.bucket_name}"

    forwarded_values {
      query_string = true

      cookies {
        forward = "none"
      }

      headers = ["Origin"]
    }

    viewer_protocol_policy = "allow-all"
    min_ttl                = 0
    default_ttl            = 86400
    max_ttl                = 31536000
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = aws_acm_certificate_validation.cert_validation.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2018"
  }

  tags = local.tags
}

resource "aws_s3_bucket" "www_bucket" {
  bucket = "www.${local.bucket_name}"

  tags = local.tags
}

resource "aws_s3_bucket_acl" "www_bucket" {
  bucket = aws_s3_bucket.www_bucket.bucket
  acl = "public-read"
}

resource "aws_s3_bucket_website_configuration" "www_bucket" {
  bucket = aws_s3_bucket.www_bucket.bucket
  redirect_all_requests_to {
    protocol = "https"
    host_name = local.domain_name
  }
}

resource "aws_s3_bucket_cors_configuration" "www_bucket" {
  bucket = aws_s3_bucket.www_bucket.bucket

  cors_rule {
    allowed_headers = ["Authorization", "Content-Length"]
    allowed_methods = ["GET", "POST"]
    allowed_origins = ["https://www.${local.domain_name}"]
    max_age_seconds = 3000
  }
}

resource "aws_s3_bucket" "root_bucket" {
  bucket = local.bucket_name

  tags = local.tags
}

resource "aws_s3_bucket_acl" "root_bucket" {
  bucket = aws_s3_bucket.root_bucket.bucket
  acl = "public-read"
}

resource "aws_s3_bucket_website_configuration" "root_bucket" {
  bucket = aws_s3_bucket.root_bucket.bucket

  index_document {
    suffix = "index.html"
  }

  error_document {
    key = "404.html"
  }
}

resource "aws_s3_bucket_cors_configuration" "root_bucket" {
  bucket = aws_s3_bucket.www_bucket.bucket

  cors_rule {
    allowed_headers = ["Authorization", "Content-Length"]
    allowed_methods = ["GET", "POST"]
    allowed_origins = ["https://${local.domain_name}"]
    max_age_seconds = 3000
  }
}


data "aws_iam_policy_document" "www_bucket" {
  statement {
    sid = "${local.deployment.site}-${local.environment}-policy"
    effect = "Allow"
    actions = [
      "s3:GetObject"
    ]
    resources = [
      "arn:aws:s3:::www.${local.bucket_name}/*"
    ]
    principals {
      identifiers = [
        "*"
      ]
      type = "AWS"
    }
  }
}

resource "aws_s3_bucket_policy" "www_bucket" {
  bucket = aws_s3_bucket.www_bucket.id
  policy = data.aws_iam_policy_document.www_bucket.json
}


data "aws_iam_policy_document" "root_bucket" {
  statement {
    sid = "${local.deployment.site}-${local.environment}-policy"
    effect = "Allow"
    actions = [
      "s3:GetObject"
    ]
    resources = [
      "arn:aws:s3:::${local.bucket_name}/*"
    ]
    principals {
      identifiers = [
        "*"
      ]
      type = "AWS"
    }
  }
}

resource "aws_s3_bucket_policy" "root_bucket" {
  bucket = aws_s3_bucket.root_bucket.id
  policy = data.aws_iam_policy_document.root_bucket.json
}

