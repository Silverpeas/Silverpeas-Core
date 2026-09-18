# Coverity Static Analysis Setup Guide for Silverpeas Core

This document provides comprehensive instructions for setting up and using Coverity static application security testing (SAST) for the Silverpeas Core project.

## Table of Contents

- [Overview](#overview)
- [Platform Support](#platform-support)
- [Prerequisites](#prerequisites)
- [Required Secrets and Variables](#required-secrets-and-variables)
- [Workflow Configuration](#workflow-configuration)
- [Analysis Configuration](#analysis-configuration)
- [Usage](#usage)
- [Results and Reporting](#results-and-reporting)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)
- [Integration with Existing Tools](#integration-with-existing-tools)

## Overview

Coverity provides comprehensive static application security testing (SAST) capabilities that analyze source code for:

- **Security Vulnerabilities**: SQL injection, XSS, command injection, cryptographic issues
- **Quality Issues**: Resource leaks, null pointer dereferences, uninitialized variables
- **Concurrency Problems**: Race conditions, deadlocks, synchronization issues
- **Web Application Security**: CSRF, session fixation, input validation issues
- **Code Quality**: Dead code, copy-paste errors, coding standard violations

## Platform Support

Based on the [Coverity Platform Support documentation](https://documentation.blackduck.com/bundle/coverity-docs/page/deploy-install-guide/topics/supported_platforms_for_coverity_analysis.html#ca_platform_support__macos-cov-analysis-platform-support), our configuration supports:

### Supported Platforms
- **Linux x86_64** (Primary CI/CD environment)
- **macOS** (Developer workstations)
- **Windows** (Developer workstations)

### Java Support
- **Java 21** (LTS) - Primary version used in Silverpeas Core
- **Maven 3.6+** - Build system
- **JSP/Servlet Analysis** - Web application components

### Analysis Capabilities
- **Full Static Analysis** - Complete codebase analysis
- **Incremental Analysis** - Analyze only changed code
- **Preview Analysis** - Lightweight analysis for rapid feedback

## Prerequisites

### Coverity License and Server
- Valid Coverity license with analysis capabilities
- Access to Coverity Connect server (on-premises or cloud)
- Appropriate user permissions for project creation and stream management

### Repository Requirements
- Admin access to GitHub repository
- GitHub Actions enabled
- Maven-based Java project (✅ already present)

### System Requirements
- GitHub Actions runners with sufficient resources
- Network connectivity to Coverity Connect server
- Adequate storage for analysis artifacts

## Required Secrets and Variables

### GitHub Repository Variables

Navigate to `Settings > Secrets and variables > Actions > Variables`:

| Variable Name | Value | Description |
|---------------|-------|-------------|
| `COVERITY_URL` | `https://your-coverity-server.com` | Coverity Connect server URL |

### GitHub Repository Secrets

Navigate to `Settings > Secrets and variables > Actions > Secrets`:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `COVERITY_USERNAME` | `your-coverity-username` | Coverity Connect username |
| `COVERITY_PASSWORD` | `your-coverity-password` | Coverity Connect password or API key |

### Creating Coverity Credentials

#### For Coverity Connect Server:
1. Log into Coverity Connect
2. Navigate to **Administration > Users**
3. Create service account for CI/CD:
   - Username: `github-actions-silverpeas`
   - Role: **Developer** or **Project Manager**
   - Permissions: **Commit**, **View**, **Triage**

#### For API Token (Recommended):
1. In Coverity Connect, go to **User Profile**
2. Generate API token
3. Use token as `COVERITY_PASSWORD` (leave username as is)

## Workflow Configuration

### Workflow Files

1. **`.github/workflows/coverity-analysis.yml`** - Main Coverity workflow
2. **`coverity-config.yml`** - Detailed analysis configuration

### Workflow Triggers

- **Push Events**: Main branches (`main`, `master`, `develop`, `stage`, `release`)
- **Pull Requests**: All PRs to main branches
- **Manual Execution**: Via GitHub Actions UI with analysis type selection

### Analysis Types

#### Full Analysis
- **Trigger**: Push to main branches
- **Duration**: 60-180 minutes
- **Scope**: Complete codebase analysis
- **Checkers**: All security, quality, and concurrency checkers

#### Pull Request Analysis  
- **Trigger**: PR creation/updates
- **Duration**: 30-90 minutes
- **Scope**: Full analysis with PR-specific reporting
- **Focus**: Security and critical quality issues

#### Preview Analysis
- **Trigger**: Draft PRs or manual selection
- **Duration**: 15-45 minutes
- **Scope**: Lightweight analysis for rapid feedback
- **Focus**: High-confidence security issues

## Analysis Configuration

### Project Structure Analysis

The configuration is optimized for Silverpeas Core's multi-module structure:

```
├── core-api/          # High priority - API definitions
├── core-library/      # High priority - Core functionality  
├── core-services/     # High priority - Business services
├── core-web/          # High priority - Web components
├── core-war/          # Medium priority - Web application
├── core-jcr/          # Medium priority - Content repository
├── core-rs/           # Medium priority - REST services
└── core-configuration/ # Low priority - Configuration
```

### Security Checkers Enabled

#### High-Priority Security Issues
- **Injection Flaws**: SQL, Command, LDAP, XPath injection
- **Cross-Site Scripting (XSS)**: Reflected and stored XSS
- **Cryptographic Issues**: Weak algorithms, hardcoded credentials
- **Input Validation**: Path manipulation, unsafe redirects

#### Web Application Security
- **CSRF Protection**: Cross-site request forgery
- **Session Management**: Session fixation, improper invalidation
- **Authentication**: Weak authentication mechanisms
- **Authorization**: Access control bypasses

#### Quality and Reliability
- **Resource Management**: Memory leaks, file handle leaks
- **Null Pointer**: Dereferences and null return issues
- **Concurrency**: Race conditions, deadlocks, synchronization
- **Logic Errors**: Dead code, infinite loops, copy-paste errors

### Module-Specific Configuration

Each module has tailored checker configurations based on functionality:

- **core-api**: API usage errors, resource management
- **core-services**: Security checkers, business logic validation
- **core-web**: Web security, XSS protection, input validation
- **core-war**: Comprehensive web application security

## Usage

### Automatic Analysis

#### Push to Main Branches
```bash
git push origin main
# Triggers full Coverity analysis
```

#### Pull Request Creation
```bash
git checkout -b feature/security-improvement
# Make changes
git push origin feature/security-improvement
# Open PR - triggers PR analysis
```

### Manual Analysis

1. Navigate to **Actions** tab in GitHub repository
2. Select **Coverity Static Analysis** workflow
3. Click **Run workflow**
4. Select analysis type:
   - **FULL**: Complete analysis
   - **INCREMENTAL**: Analyze only changes
   - **PREVIEW**: Lightweight analysis
5. Click **Run workflow**

### Local Development Integration

#### Using Coverity Desktop (Optional)
```bash
# Install Coverity Desktop on developer machine
cov-build --dir idir mvn clean compile
cov-analyze --dir idir --webapp-security
cov-format-errors --dir idir --html-output local-results
```

## Results and Reporting

### GitHub Integration

#### Security Tab
- SARIF results uploaded to GitHub Security
- Navigate to **Security > Code scanning alerts**
- View vulnerabilities by severity and category

#### Pull Request Comments
- Automated comments on PRs with analysis summary
- Issue count by severity (High/Medium/Low)
- Links to detailed reports

#### Workflow Artifacts
- **HTML Report**: Interactive web-based results
- **JSON Data**: Machine-readable results
- **Text Report**: Command-line friendly format
- **SARIF**: GitHub Security integration format

### Coverity Connect Dashboard

#### Project View
1. Access Coverity Connect server
2. Navigate to Projects
3. Select **Silverpeas-Core-{branch}** stream
4. Review:
   - **Outstanding Issues**: Unresolved defects
   - **Impact Analysis**: High/Medium/Low classification
   - **Quality Metrics**: Defect density, code coverage
   - **Trends**: Issue introduction/resolution over time

#### Detailed Analysis
- **Issue Details**: Root cause, impact, remediation
- **Code Context**: Exact location and data flow
- **False Positive Management**: Mark and suppress non-issues
- **Workflow Integration**: Assign, triage, track resolution

### Quality Gates

#### Build Failure Conditions
- **High-Impact Issues**: Any new high-impact security issues fail the build
- **Critical Vulnerabilities**: Configurable threshold (default: 0)
- **Quality Thresholds**: Maximum total issues per module

#### Notification System
- **PR Comments**: Real-time feedback on pull requests
- **Email Alerts**: Optional email notifications
- **Webhook Integration**: Custom notification endpoints

## Troubleshooting

### Common Issues

#### 1. Tool Download Failures
```
Error: Failed to download Coverity tools
```
**Solutions**:
- Verify `COVERITY_URL` is accessible from GitHub Actions
- Check `COVERITY_USERNAME` and `COVERITY_PASSWORD` credentials
- Ensure network connectivity to Coverity server
- Verify download permissions in Coverity Connect

#### 2. Build Capture Problems
```
Error: No files captured for analysis
```
**Solutions**:
- Verify Maven build succeeds independently
- Check Java classpath and dependencies
- Review include/exclude path configurations
- Ensure source files are being compiled

#### 3. Analysis Failures
```
Error: Analysis failed with checker errors
```
**Solutions**:
- Review checker configuration in `coverity-config.yml`
- Check Java version compatibility (ensure Java 21)
- Verify analysis options are supported by your Coverity version
- Review intermediate directory permissions

#### 4. Stream Connection Issues
```
Error: Cannot commit to stream
```
**Solutions**:
- Verify stream exists in Coverity Connect
- Check user permissions for stream access
- Validate server URL and credentials
- Review network connectivity and firewall rules

#### 5. Memory and Performance Issues
```
Error: Analysis timeout or out of memory
```
**Solutions**:
- Increase GitHub Actions timeout values
- Adjust memory allocation in workflow
- Use incremental analysis for large codebases
- Optimize checker selection for faster analysis

### Debug Mode

Enable detailed logging by modifying the workflow:

```yaml
- name: Enable Debug Logging
  run: |
    export COV_DEBUG=1
    export COV_VERBOSE=1
    # Run analysis commands
```

### Log Analysis

Review logs in GitHub Actions:
1. Navigate to failed workflow run
2. Expand analysis steps
3. Look for specific error patterns:
   - **Build capture**: `cov-build` output
   - **Analysis**: `cov-analyze` errors  
   - **Commit**: `cov-commit-defects` issues

## Best Practices

### Security Analysis

#### 1. Regular Scanning
- **Daily Scans**: Automated scans on every push
- **Weekly Baseline**: Comprehensive analysis including test code
- **Release Scans**: Full security validation before releases

#### 2. Issue Triage
- **Immediate Response**: Address high-impact security issues within 24 hours
- **Prioritization**: Focus on exploitable vulnerabilities first
- **False Positive Management**: Maintain suppression database

#### 3. Developer Training
- **Security Awareness**: Train developers on common vulnerability patterns
- **Tool Usage**: Provide training on interpreting Coverity results
- **Remediation Techniques**: Share best practices for fixing common issues

### Performance Optimization

#### 1. Analysis Efficiency
```yaml
# Optimize build capture
cov-build:
  - Use minimal build commands
  - Exclude test compilation
  - Enable parallel analysis

# Incremental analysis for large projects
incremental_analysis:
  - Enable for PR analysis
  - Use baseline comparisons
  - Focus on changed files
```

#### 2. Resource Management
- **Parallel Jobs**: Utilize multiple CPU cores
- **Memory Allocation**: Adjust based on project size
- **Caching**: Cache tools and intermediate results
- **Timeout Configuration**: Set appropriate limits

#### 3. Selective Analysis
```yaml
# Module-specific analysis
modules:
  core-web:
    checkers: ["WEBAPP", "XSS", "CSRF"]
  core-api: 
    checkers: ["API_USAGE", "RESOURCE_LEAK"]
```

### Quality Management

#### 1. Baseline Management
- **Establish Baseline**: Set initial quality metrics
- **Track Trends**: Monitor improvement over time
- **Set Targets**: Define quality goals and thresholds

#### 2. Continuous Improvement
- **Regular Reviews**: Weekly security team reviews
- **Metrics Analysis**: Track defect density and resolution rates
- **Process Refinement**: Continuously improve analysis configuration

#### 3. Integration with Development Workflow
- **Pre-commit Hooks**: Optional local analysis before commits
- **IDE Integration**: Coverity plugin for development environments
- **Code Review**: Include security analysis in review process

## Integration with Existing Tools

### Black Duck Integration

The Coverity analysis complements existing Black Duck scanning:

| Tool | Focus | Coverage |
|------|-------|----------|
| **Coverity** | Static code analysis | Source code security and quality |
| **Black Duck** | Software composition | Open source vulnerabilities |
| **Polaris** | Combined analysis | Both static and composition |

### Workflow Coordination

```yaml
# Example: Run both analyses
name: Security Analysis
jobs:
  coverity:
    uses: ./.github/workflows/coverity-analysis.yml
  blackduck:
    uses: ./.github/workflows/black-duck-bridge.yml
  security-summary:
    needs: [coverity, blackduck]
    # Combine results
```

### Jenkins Integration

For existing Jenkins pipelines:

```groovy
pipeline {
  stages {
    stage('Coverity Analysis') {
      steps {
        // Call GitHub Actions or run Coverity directly
        sh 'cov-build --dir idir mvn compile'
        sh 'cov-analyze --dir idir --security'
        publishCoverityResults()
      }
    }
  }
}
```

## Maintenance and Updates

### Regular Tasks

#### Weekly
- [ ] Review new high-impact issues
- [ ] Update suppression database
- [ ] Check analysis performance metrics
- [ ] Review quality gate thresholds

#### Monthly  
- [ ] Update Coverity tools (if self-managed)
- [ ] Review and optimize checker configurations
- [ ] Analyze security metrics trends
- [ ] Update documentation as needed

#### Quarterly
- [ ] Assess overall security posture
- [ ] Review and update security policies
- [ ] Evaluate new Coverity features
- [ ] Conduct security training sessions

### Tool Updates

#### Coverity Tool Updates
1. Test new versions in staging environment
2. Review compatibility with Java 21 and Maven
3. Update workflow tool download URLs
4. Validate checker behavior changes

#### Configuration Updates
1. Review and update `coverity-config.yml`
2. Adjust quality gates based on project maturity
3. Update checker selections based on vulnerability trends
4. Optimize performance settings

### Monitoring and Metrics

#### Key Performance Indicators
- **Analysis Success Rate**: >95% successful scans
- **Time to Resolution**: <48 hours for high-impact issues
- **False Positive Rate**: <10% of reported issues
- **Coverage Metrics**: >80% code coverage by analysis

#### Alerting
- **Analysis Failures**: Immediate notification
- **New High-Impact Issues**: Within 1 hour
- **Quality Gate Violations**: Real-time feedback
- **System Performance**: Weekly performance reports

---

## Support and Resources

- **Coverity Documentation**: [Synopsys Coverity Docs](https://documentation.blackduck.com/)
- **GitHub Actions**: Workflow run logs and artifacts
- **Internal Support**: Security team contact information
- **Training Materials**: Coverity University and certification programs

For additional assistance, contact the Security Engineering team or create an issue in this repository.